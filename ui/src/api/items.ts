import { http } from './client';
import type { ModOption, PageResponse } from './types';
import type { ItemDetail, ItemListParams, NeiPanelEntry } from './items.types';

export async function listItems(
  datasetId: string,
  params: ItemListParams,
): Promise<PageResponse<NeiPanelEntry>> {
  const { data } = await http.get<PageResponse<NeiPanelEntry>>(
    `/datasets/${encodeURIComponent(datasetId)}/items`,
    { params },
  );
  return data;
}

export async function getItemDetail(datasetId: string, itemVariantId: string): Promise<ItemDetail> {
  const { data } = await http.get<ItemDetail>(
    `/datasets/${encodeURIComponent(datasetId)}/items/${encodeURIComponent(itemVariantId)}`,
  );
  return data;
}

export async function listMods(datasetId: string): Promise<ModOption[]> {
  const { data } = await http.get<ModOption[]>(`/datasets/${encodeURIComponent(datasetId)}/mods`);
  return data;
}
