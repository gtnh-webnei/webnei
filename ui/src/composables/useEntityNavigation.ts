import type { Router } from 'vue-router';
import type { ComputedRef, Ref } from 'vue';

type DatasetIdSource = string | Ref<string> | ComputedRef<string>;

type EntityPayload = {
  itemVariantId?: string | null;
  fluidVariantId?: string | null;
};

type EntityTarget = EntityPayload | string | null | undefined;

function currentDatasetId(datasetId: DatasetIdSource): string {
  return typeof datasetId === 'string' ? datasetId : datasetId.value;
}

function targetFromPayload(payload: EntityTarget): string | null {
  if (!payload) return null;
  if (typeof payload === 'string') return payload;
  return payload.itemVariantId ?? payload.fluidVariantId ?? null;
}

export function useEntityNavigation(router: Router, datasetId: DatasetIdSource) {
  function go(kind: 'detail' | 'recipe' | 'usage', payload: EntityTarget, replace = false) {
    const target = targetFromPayload(payload);
    if (!target) return;
    const location = {
      name: 'lookup',
      params: { datasetId: currentDatasetId(datasetId) },
      query: { target, kind },
    };
    if (replace) {
      router.replace(location);
    } else {
      router.push(location);
    }
  }

  function pick(payload: EntityTarget, replace = false) {
    go('detail', payload, replace);
  }

  function lookup(next: 'recipe' | 'usage', payload: EntityTarget, replace = false) {
    go(next, payload, replace);
  }

  return { pick, lookup };
}
