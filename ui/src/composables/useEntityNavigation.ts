import type { Router } from 'vue-router';
import type { ComputedRef, Ref } from 'vue';

type DatasetIdSource = string | Ref<string> | ComputedRef<string>;

type EntityPayload = {
  itemVariantId: string | null;
  fluidVariantId: string | null;
};

function currentDatasetId(datasetId: DatasetIdSource): string {
  return typeof datasetId === 'string' ? datasetId : datasetId.value;
}

function targetFromPayload(payload: EntityPayload): string | null {
  return payload.itemVariantId ?? payload.fluidVariantId;
}

export function useEntityNavigation(router: Router, datasetId: DatasetIdSource) {
  function pick(payload: EntityPayload, replace = false) {
    const target = targetFromPayload(payload);
    if (!target) return;
    const location = {
      name: 'lookup',
      params: { datasetId: currentDatasetId(datasetId) },
      query: { target, kind: 'detail' },
    };
    if (replace) {
      router.replace(location);
    } else {
      router.push(location);
    }
  }

  function lookup(next: 'recipe' | 'usage', payload: EntityPayload) {
    const target = targetFromPayload(payload);
    if (!target) return;
    router.push({
      name: 'lookup',
      params: { datasetId: currentDatasetId(datasetId) },
      query: { target, kind: next },
    });
  }

  return { pick, lookup };
}
