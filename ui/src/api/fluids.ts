import { http } from './client';
import type { ModOption, PageResponse } from './types';
import type { FluidDetail, FluidListParams, FluidSummary } from './fluids.types';

export async function listFluids(
  datasetId: string,
  params: FluidListParams,
): Promise<PageResponse<FluidSummary>> {
  const { data } = await http.get<PageResponse<FluidSummary>>(
    `/datasets/${encodeURIComponent(datasetId)}/fluids`,
    { params },
  );
  return data;
}

export async function getFluidDetail(
  datasetId: string,
  fluidVariantId: string,
): Promise<FluidDetail> {
  const { data } = await http.get<FluidDetail>(
    `/datasets/${encodeURIComponent(datasetId)}/fluids/${encodeURIComponent(fluidVariantId)}`,
  );
  return data;
}

export async function listFluidMods(datasetId: string): Promise<ModOption[]> {
  const { data } = await http.get<ModOption[]>(
    `/datasets/${encodeURIComponent(datasetId)}/fluid-mods`,
  );
  return data;
}
