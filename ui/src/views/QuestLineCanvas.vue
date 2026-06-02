<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { storeToRefs } from 'pinia';
import { Graph } from '@antv/g6';
import { getQuestDetail, getQuestLineDetail } from '@/api/quests';
import { useThemeStore } from '@/stores/theme';
import QuestText from '@/components/QuestText.vue';
import type { QuestDetail, QuestLineDetail, QuestNode } from '@/api/quests.types';

const route = useRoute();
const router = useRouter();
const themeStore = useThemeStore();
const { effective: themeMode } = storeToRefs(themeStore);

const { t } = useI18n();

// 配色表:画布(light/dark 双套)
const palette = computed(() => {
  if (themeMode.value === 'dark') {
    return {
      background: '#1f2329',
      nodeFill: '#2a2f37',
      nodeStroke: '#475569',
      edgeStroke: '#64748b',
      edgeInbound: '#60a5fa',
      edgeOutbound: '#fb923c',
      tooltipBg: '#2a2f37',
      tooltipText: '#e5e7eb',
      tooltipSub: '#94a3b8',
    };
  }
  return {
    background: '#f8fafc',
    nodeFill: '#ffffff',
    nodeStroke: '#94a3b8',
    edgeStroke: '#94a3b8',
    edgeInbound: '#3b82f6',
    edgeOutbound: '#f97316',
    tooltipBg: '#ffffff',
    tooltipText: '#111111',
    tooltipSub: '#64748b',
  };
});

const datasetId = computed(() => String(route.params.datasetId ?? ''));
const lineId = computed(() => String(route.query.id ?? ''));

const lineDetail = ref<QuestLineDetail | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const drawerOpen = ref(false);
const selectedQuestId = ref<string | null>(null);
const questDetail = ref<QuestDetail | null>(null);
const questLoading = ref(false);
const questError = ref<string | null>(null);

const containerRef = ref<HTMLElement | null>(null);
const graph = shallowRef<Graph | null>(null);

const nodeMap = computed(() => {
  const m = new Map<string, QuestNode>();
  for (const n of lineDetail.value?.nodes ?? []) m.set(n.questId, n);
  return m;
});

async function loadLine() {
  if (!datasetId.value || !lineId.value) return;
  loading.value = true;
  error.value = null;
  try {
    lineDetail.value = await getQuestLineDetail(datasetId.value, lineId.value);
    await renderGraph();
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}

async function renderGraph() {
  if (!containerRef.value || !lineDetail.value) return;

  if (graph.value) {
    graph.value.destroy();
    graph.value = null;
  }

  const detail = lineDetail.value;
  const nodeIdSet = new Set(detail.nodes.map((n) => n.questId));

  // 预加载所有节点图标 — G6 v5 不会等图片 onload 后重绘,
  // 不预热的话首屏渲染时部分节点是空白。
  await Promise.all(
    detail.nodes
      .map((n) => n.iconAssetUrl)
      .filter((url): url is string => !!url)
      .map(
        (url) =>
          new Promise<void>((resolve) => {
            const img = new Image();
            img.onload = () => resolve();
            img.onerror = () => resolve();
            img.src = url;
          }),
      ),
  );

  // 每个节点用 rect 作底(有边框 + 背景),前景 icon 通过 iconSrc 渲染。
  // 这样能解决 image 节点没边框的问题,也能控制图标的清晰度。
  // G6 v5 的 NodeData/EdgeData 类型很严格(Cursor 枚举、Vector tuple 等),
  // 这里数据形态由我们手工拼,跳过严格类型用 any。
  const nodes: any[] = detail.nodes.map((n) => {
    const cx = n.posX + n.sizeX / 2;
    const cy = n.posY + n.sizeY / 2;
    return {
      id: n.questId,
      type: 'rect',
      style: {
        x: cx,
        y: cy,
        // G6 v5 rect 的 size 是节点宽/高(以 [x,y] 为中心向两侧延伸 size/2)。
        // BQ 任务节点 sizeX/sizeY 已经包含完整边框,直接照搬保持 pos 间距与
        // BQ 内一致 — 之前 +4 把节点撑大,挤掉了节点之间的缝隙。
        size: [n.sizeX, n.sizeY],
        fill: palette.value.nodeFill,
        stroke: palette.value.nodeStroke,
        lineWidth: 1.5,
        radius: 3,
        cursor: 'pointer',
        iconSrc: n.iconAssetUrl ?? undefined,
        iconWidth: n.sizeX - 4,
        iconHeight: n.sizeY - 4,
      },
      data: { name: n.name },
    };
  });

  const edges: any[] = detail.edges
    .filter((e) => nodeIdSet.has(e.requiredQuestId) && nodeIdSet.has(e.questId))
    .map((e, idx) => ({
      id: `edge-${idx}`,
      source: e.requiredQuestId,
      target: e.questId,
      type: 'line',
      style: {
        stroke: palette.value.edgeStroke,
        lineWidth: 1.2,
        endArrow: true,
        endArrowSize: 5,
        opacity: 0.55,
      },
    }));

  const g = new Graph({
    container: containerRef.value,
    autoFit: 'view',
    padding: 48,
    background: palette.value.background,
    behaviors: ['drag-canvas', 'zoom-canvas'],
    edge: {
      // hover 节点时区分入边 / 出边:
      //   inbound(前置依赖,指向该节点) — 蓝色
      //   outbound(后续解锁,从该节点发出) — 橙色
      state: {
        inbound: {
          stroke: palette.value.edgeInbound,
          lineWidth: 1.6,
          opacity: 0.9,
        },
        outbound: {
          stroke: palette.value.edgeOutbound,
          lineWidth: 1.6,
          opacity: 0.9,
        },
      },
    },
    plugins: [
      {
        type: 'tooltip',
        key: 'quest-tooltip',
        trigger: 'hover',
        enable: (e: { targetType?: string }) => e?.targetType === 'node',
        // 用 style 把 G6 默认 tooltip 外壳改成主题色,自定义内容只放纯文本
        style: {
          '.tooltip': {
            background: palette.value.tooltipBg,
            color: palette.value.tooltipText,
            border: `1px solid ${palette.value.nodeStroke}`,
            'box-shadow':
              themeMode.value === 'dark'
                ? '0 6px 16px rgba(0,0,0,0.4)'
                : '0 6px 12px rgba(0,0,0,0.12)',
          },
        },
        getContent: (_evt: unknown, items: Array<{ data: { name?: string } }>) => {
          if (!items?.length) return '';
          const it = items[0];
          const name = it.data?.name ?? '';
          return `<div style="font-weight:600;font-size:13px;line-height:1.45;max-width:240px">${escapeHtml(name)}</div>`;
        },
      },
    ],
    data: { nodes, edges },
  });

  g.on('node:click', (evt: unknown) => {
    const id = (evt as { target?: { id?: string } })?.target?.id;
    if (id) openQuest(id);
  });

  // hover 节点 → 入边 outbound、出边 inbound 分别染色
  // edge.source 是前置任务(必须先完成),edge.target 是后置任务。
  // 当前节点视角:
  //   该 node 作为 target → 入边(指向我的前置) → 'inbound'
  //   该 node 作为 source → 出边(从我解锁的后续) → 'outbound'
  const inboundEdges = new Map<string, string[]>(); // node → 入边 ids
  const outboundEdges = new Map<string, string[]>(); // node → 出边 ids
  for (const e of edges) {
    if (!inboundEdges.has(e.target)) inboundEdges.set(e.target, []);
    if (!outboundEdges.has(e.source)) outboundEdges.set(e.source, []);
    inboundEdges.get(e.target)!.push(e.id);
    outboundEdges.get(e.source)!.push(e.id);
  }
  let activeEdgeIds: string[] = [];

  g.on('node:pointerenter', (evt: unknown) => {
    const id = (evt as { target?: { id?: string } })?.target?.id;
    if (!id) return;
    const ins = inboundEdges.get(id) ?? [];
    const outs = outboundEdges.get(id) ?? [];
    if (!ins.length && !outs.length) return;
    const states: Record<string, string[]> = {};
    for (const eid of ins) states[eid] = ['inbound'];
    for (const eid of outs) states[eid] = ['outbound'];
    activeEdgeIds = [...ins, ...outs];
    g.setElementState(states);
  });

  g.on('node:pointerleave', () => {
    if (activeEdgeIds.length) {
      const states: Record<string, string[]> = {};
      for (const eid of activeEdgeIds) states[eid] = [];
      g.setElementState(states);
      activeEdgeIds = [];
    }
  });

  await g.render();

  // 图标像素化 — Minecraft 贴图是 16x16/32x32 像素艺术,smooth 会糊。
  // G6 v5 在 canvas 上画图片用浏览器的 drawImage,设置 imageSmoothingEnabled=false
  // 来保持像素感。
  const canvas = containerRef.value?.querySelector('canvas') as HTMLCanvasElement | null;
  if (canvas) {
    const ctx = canvas.getContext('2d');
    if (ctx) ctx.imageSmoothingEnabled = false;
  }

  graph.value = g;
}

function escapeHtml(s: string): string {
  return s
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

async function openQuest(questId: string) {
  selectedQuestId.value = questId;
  drawerOpen.value = true;
  questLoading.value = true;
  questError.value = null;
  questDetail.value = null;
  try {
    questDetail.value = await getQuestDetail(datasetId.value, questId);
  } catch (e) {
    questError.value = e instanceof Error ? e.message : String(e);
  } finally {
    questLoading.value = false;
  }
}

function goToItem(itemVariantId: string) {
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: itemVariantId, kind: 'detail' },
  });
}

function back() {
  router.back();
}

watch([datasetId, lineId], loadLine);
// 主题切换时 G6 没法只改 Graph 选项里的 background 与 edge.state 配色,
// 简单可靠的做法是直接拿当前 lineDetail 重渲一次 — renderGraph 会 destroy 旧图。
watch(themeMode, () => {
  if (lineDetail.value) renderGraph();
});
onMounted(loadLine);

// 画布工具栏:适应 / 1:1 / + / - / 居中。
// G6 v5 自带 fitView / fitCenter / zoomTo / zoomBy 全部异步 + 可选动画。
const ZOOM_STEP = 1.2;
const ZOOM_ANIM = { duration: 200 } as const;
function canvasFit() {
  graph.value?.fitView(undefined, ZOOM_ANIM);
}
function canvasCenter() {
  graph.value?.fitCenter(ZOOM_ANIM);
}
function canvasReset() {
  // 1:1 像素,然后居中 — 与 BQ 客户端默认观感对齐。
  graph.value?.zoomTo(1, ZOOM_ANIM)?.then(() => graph.value?.fitCenter(ZOOM_ANIM));
}
function canvasZoomIn() {
  graph.value?.zoomBy(ZOOM_STEP, ZOOM_ANIM);
}
function canvasZoomOut() {
  graph.value?.zoomBy(1 / ZOOM_STEP, ZOOM_ANIM);
}
onBeforeUnmount(() => {
  if (graph.value) {
    graph.value.destroy();
    graph.value = null;
  }
});

const taskTypeLabels = computed<Record<string, string>>(() => ({
  retrieval: t('quest.taskType.retrieval'),
  crafting: t('quest.taskType.crafting'),
  hunt: t('quest.taskType.hunt'),
  checkbox: t('quest.taskType.checkbox'),
  location: t('quest.taskType.location'),
  fluid: t('quest.taskType.fluid'),
  unhandled: t('quest.taskType.unhandled'),
}));

const rewardTypeLabels = computed<Record<string, string>>(() => ({
  item: t('quest.rewardType.item'),
  choice: t('quest.rewardType.choice'),
  xp: t('quest.rewardType.xp'),
  'complete quest': t('quest.rewardType.completeQuest'),
}));
</script>

<template>
  <div class="quest-line-canvas">
    <header class="header">
      <el-button text @click="back">{{ $t('common.back') }}</el-button>
      <div class="title-block">
        <h1>{{ lineDetail?.line?.name ?? $t('common.loading') }}</h1>
        <p v-if="lineDetail" class="lead">
          {{ $t('quest.nodeTaskCount', { count: lineDetail.nodes.length }) }} ·
          {{ $t('quest.nodeEdgeCount', { count: lineDetail.edges.length }) }}
        </p>
      </div>
    </header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />

    <div class="canvas-frame">
      <div v-if="loading" class="canvas-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div ref="containerRef" class="canvas-host" />
      <div
        v-if="lineDetail"
        class="canvas-toolbar"
        role="toolbar"
        :aria-label="$t('quest.canvasToolbar')"
      >
        <el-tooltip :content="$t('quest.fitWindow')" placement="left">
          <button type="button" class="ct-btn" @click="canvasFit">
            <svg
              viewBox="0 0 24 24"
              width="16"
              height="16"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <polyline points="4 9 4 4 9 4" />
              <polyline points="20 9 20 4 15 4" />
              <polyline points="4 15 4 20 9 20" />
              <polyline points="20 15 20 20 15 20" />
            </svg>
          </button>
        </el-tooltip>
        <el-tooltip :content="$t('quest.resetZoom')" placement="left">
          <button type="button" class="ct-btn" @click="canvasReset">1:1</button>
        </el-tooltip>
        <el-tooltip :content="$t('quest.centerGraph')" placement="left">
          <button type="button" class="ct-btn" @click="canvasCenter">
            <svg
              viewBox="0 0 24 24"
              width="16"
              height="16"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle cx="12" cy="12" r="9" />
              <circle cx="12" cy="12" r="3" />
            </svg>
          </button>
        </el-tooltip>
        <div class="ct-divider" />
        <el-tooltip :content="$t('quest.zoomIn')" placement="left">
          <button type="button" class="ct-btn" @click="canvasZoomIn">
            <svg
              viewBox="0 0 24 24"
              width="16"
              height="16"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
          </button>
        </el-tooltip>
        <el-tooltip :content="$t('quest.zoomOut')" placement="left">
          <button type="button" class="ct-btn" @click="canvasZoomOut">
            <svg
              viewBox="0 0 24 24"
              width="16"
              height="16"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
          </button>
        </el-tooltip>
      </div>
    </div>

    <el-drawer v-model="drawerOpen" direction="rtl" size="480px" :with-header="false">
      <div class="drawer-body">
        <el-skeleton v-if="questLoading" :rows="6" animated />
        <el-alert v-else-if="questError" :title="questError" type="error" :closable="false" />
        <template v-else-if="questDetail">
          <header class="quest-hero">
            <div class="quest-icon">
              <img v-if="questDetail.node.iconAssetUrl" :src="questDetail.node.iconAssetUrl" />
            </div>
            <div class="quest-title">
              <h2>{{ questDetail.node.name }}</h2>
              <div class="quest-meta">
                <el-tag
                  v-if="questDetail.node.repeatTime > 0"
                  size="small"
                  type="info"
                  effect="plain"
                  round
                >
                  {{ $t('quest.repeatable') }}
                </el-tag>
              </div>
            </div>
          </header>

          <QuestText
            v-if="questDetail.node.description"
            :text="questDetail.node.description"
            class="quest-desc"
          />

          <section v-if="questDetail.tasks.length" class="quest-section">
            <div class="section-title">
              {{ $t('quest.taskConditions', { count: questDetail.tasks.length }) }}
            </div>
            <div class="task-list">
              <div v-for="task in questDetail.tasks" :key="task.taskId" class="task-card">
                <div class="task-header">
                  <el-tag size="small" type="primary" effect="plain" round>
                    {{ taskTypeLabels[task.taskType] ?? task.taskType }}
                  </el-tag>
                  <span class="task-name">{{ task.name }}</span>
                  <span v-if="task.numberRequired > 0" class="task-num"
                    >×{{ task.numberRequired }}</span
                  >
                </div>
                <div v-if="task.itemGroups.length" class="task-items">
                  <div
                    v-for="g in task.itemGroups"
                    :key="`tg-${task.taskId}-${g.listIndex}`"
                    class="item-group"
                  >
                    <div
                      v-for="(e, idx) in g.entries"
                      :key="`te-${task.taskId}-${g.listIndex}-${idx}`"
                      class="item-chip"
                      :title="e.displayName ?? e.itemVariantId ?? e.fluidVariantId ?? ''"
                      @click="e.itemVariantId && goToItem(e.itemVariantId)"
                    >
                      <img v-if="e.assetUrl" :src="e.assetUrl" loading="lazy" />
                      <span class="item-name">{{
                        e.displayName ?? e.itemVariantId ?? e.fluidVariantId
                      }}</span>
                      <span v-if="e.amount > 1" class="item-amount">×{{ e.amount }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="task.mobVariantId" class="task-mob">
                  {{ $t('quest.huntTarget') }}{{ task.mobVariantId }}
                </div>
                <div v-if="task.dimensionName" class="task-mob">
                  {{ $t('quest.dimension') }}{{ task.dimensionName }}
                </div>
              </div>
            </div>
          </section>

          <section v-if="questDetail.rewards.length" class="quest-section">
            <div class="section-title">
              {{ $t('quest.rewards', { count: questDetail.rewards.length }) }}
            </div>
            <div class="task-list">
              <div v-for="reward in questDetail.rewards" :key="reward.rewardId" class="task-card">
                <div class="task-header">
                  <el-tag size="small" type="warning" effect="plain" round>
                    {{ rewardTypeLabels[reward.rewardType] ?? reward.rewardType }}
                  </el-tag>
                  <span class="task-name">{{ reward.name }}</span>
                  <span v-if="reward.xp > 0" class="task-num">{{
                    reward.levels
                      ? $t('quest.xpLevel', { count: reward.xp })
                      : $t('quest.xpPoints', { count: reward.xp })
                  }}</span>
                </div>
                <div v-if="reward.itemGroups.length" class="task-items">
                  <div
                    v-for="g in reward.itemGroups"
                    :key="`rg-${reward.rewardId}-${g.listIndex}`"
                    class="item-group"
                  >
                    <div
                      v-for="(e, idx) in g.entries"
                      :key="`re-${reward.rewardId}-${g.listIndex}-${idx}`"
                      class="item-chip"
                      :title="e.displayName ?? e.itemVariantId ?? e.fluidVariantId ?? ''"
                      @click="e.itemVariantId && goToItem(e.itemVariantId)"
                    >
                      <img v-if="e.assetUrl" :src="e.assetUrl" loading="lazy" />
                      <span class="item-name">{{
                        e.displayName ?? e.itemVariantId ?? e.fluidVariantId
                      }}</span>
                      <span v-if="e.amount > 1" class="item-amount">×{{ e.amount }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </section>

          <code class="quest-id">{{ questDetail.node.questId }}</code>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.quest-line-canvas {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
}
.header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title-block h1 {
  margin: 0;
  font-size: 20px;
}
.lead {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.canvas-frame {
  flex: 1;
  min-height: 600px;
  position: relative;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}
.canvas-host {
  width: 100%;
  height: 100%;
}
.canvas-loading {
  position: absolute;
  inset: 24px;
  z-index: 1;
  background: var(--el-bg-color);
}
.canvas-toolbar {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.ct-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--el-text-color-regular);
  cursor: pointer;
  font:
    600 11px ui-monospace,
    SFMono-Regular,
    Menlo,
    monospace;
  transition:
    background 0.15s,
    color 0.15s;
}
.ct-btn:hover {
  background: var(--el-fill-color);
  color: var(--el-color-primary);
}
.ct-btn:active {
  background: var(--el-fill-color-darker);
}
.ct-divider {
  height: 1px;
  margin: 2px 4px;
  background: var(--el-border-color-lighter);
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 8px 4px;
}
.quest-hero {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.quest-icon {
  width: 56px;
  height: 56px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
}
.quest-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.quest-title h2 {
  margin: 0 0 6px 0;
  font-size: 16px;
}
.quest-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.quest-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: var(--el-text-color-regular);
  white-space: pre-wrap;
  background: var(--el-fill-color-lighter);
  padding: 10px 12px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
}
.quest-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.task-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.task-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 8px 10px;
  background: var(--el-bg-color);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.task-header {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.task-name {
  font-size: 13px;
  font-weight: 500;
}
.task-num {
  font-size: 12px;
  color: var(--el-color-primary);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  margin-left: auto;
}
.task-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.item-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.item-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 8px;
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  flex-shrink: 0;
  transition:
    border-color 0.15s,
    background 0.15s;
}
.item-chip:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.item-chip img {
  width: 32px;
  height: 32px;
  object-fit: contain;
  image-rendering: pixelated;
  flex-shrink: 0;
}
.item-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-amount {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: var(--el-color-primary);
}
.task-mob {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.quest-id {
  font-size: 10px;
  color: var(--el-text-color-disabled);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  word-break: break-all;
  margin-top: 4px;
}
</style>
