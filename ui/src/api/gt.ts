import { http } from './client';
import type {
  GtBartWorksOreDetail,
  GtBartWorksOreSummary,
  GtOreVeinDetail,
  GtOreVeinSummary,
  GtResourceListResponse,
  GtSmallOreDetail,
  GtSmallOreSummary,
  GtUndergroundFluidDetail,
  GtUndergroundFluidSummary,
} from './gt.types';

function gtBase(datasetId: string) {
  return `/datasets/${encodeURIComponent(datasetId)}/gt`;
}

function listParams(params: { q?: string; dimension?: string; type?: string }) {
  const qs: Record<string, string> = {};
  const q = params.q?.trim();
  if (q) qs.q = q;
  if (params.dimension) qs.dimension = params.dimension;
  if (params.type) qs.type = params.type;
  return qs;
}

export async function listGtOreVeins(
  datasetId: string,
  params: { q?: string; dimension?: string },
): Promise<GtResourceListResponse<GtOreVeinSummary>> {
  const { data } = await http.get<GtResourceListResponse<GtOreVeinSummary>>(
    `${gtBase(datasetId)}/ore-veins`,
    { params: listParams(params) },
  );
  return data;
}

export async function getGtOreVeinDetail(
  datasetId: string,
  veinName: string,
): Promise<GtOreVeinDetail> {
  const { data } = await http.get<GtOreVeinDetail>(
    `${gtBase(datasetId)}/ore-veins/${encodeURIComponent(veinName)}`,
  );
  return data;
}

export async function listGtSmallOres(
  datasetId: string,
  params: { q?: string; dimension?: string },
): Promise<GtResourceListResponse<GtSmallOreSummary>> {
  const { data } = await http.get<GtResourceListResponse<GtSmallOreSummary>>(
    `${gtBase(datasetId)}/small-ores`,
    { params: listParams(params) },
  );
  return data;
}

export async function getGtSmallOreDetail(
  datasetId: string,
  oreGenName: string,
): Promise<GtSmallOreDetail> {
  const { data } = await http.get<GtSmallOreDetail>(
    `${gtBase(datasetId)}/small-ores/${encodeURIComponent(oreGenName)}`,
  );
  return data;
}

export async function listGtUndergroundFluids(
  datasetId: string,
  params: { q?: string; dimension?: string },
): Promise<GtResourceListResponse<GtUndergroundFluidSummary>> {
  const { data } = await http.get<GtResourceListResponse<GtUndergroundFluidSummary>>(
    `${gtBase(datasetId)}/underground-fluids`,
    { params: listParams(params) },
  );
  return data;
}

export async function getGtUndergroundFluidDetail(
  datasetId: string,
  fluidId: string,
): Promise<GtUndergroundFluidDetail> {
  const { data } = await http.get<GtUndergroundFluidDetail>(
    `${gtBase(datasetId)}/underground-fluids/${encodeURIComponent(fluidId)}`,
  );
  return data;
}

export async function listGtBartWorksOres(
  datasetId: string,
  params: { q?: string; dimension?: string; type?: string },
): Promise<GtResourceListResponse<GtBartWorksOreSummary>> {
  const { data } = await http.get<GtResourceListResponse<GtBartWorksOreSummary>>(
    `${gtBase(datasetId)}/bartworks-ores`,
    { params: listParams(params) },
  );
  return data;
}

export async function getGtBartWorksOreDetail(
  datasetId: string,
  entryId: string,
): Promise<GtBartWorksOreDetail> {
  const { data } = await http.get<GtBartWorksOreDetail>(
    `${gtBase(datasetId)}/bartworks-ores/${encodeURIComponent(entryId)}`,
  );
  return data;
}
