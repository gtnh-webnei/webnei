import { http } from './client';
import type { FluidExtras, ItemExtras } from './extras.types';

export async function getItemExtras(datasetId: string, itemVariantId: string): Promise<ItemExtras> {
  const { data } = await http.get<ItemExtras>(
    `/datasets/${encodeURIComponent(datasetId)}/items/${encodeURIComponent(itemVariantId)}/extras`,
  );
  return data;
}

export async function getFluidExtras(
  datasetId: string,
  fluidVariantId: string,
): Promise<FluidExtras> {
  const { data } = await http.get<FluidExtras>(
    `/datasets/${encodeURIComponent(datasetId)}/fluids/${encodeURIComponent(fluidVariantId)}/extras`,
  );
  return data;
}
