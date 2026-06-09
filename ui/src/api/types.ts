export interface DatasetSummary {
  datasetId: string;
  packSlug: string;
  packVersion: string;
  variant: string;
  language: string;
  displayName: string;
  schemaVersion: string;
  exporterVersion: string;
  minecraftVersion: string;
  createdAt: string;
  activePlugins: string[];
  /** 外挂语言无关 spec URL，形如 /assets/gtnh/2.8.4/official/spec/display.json */
  displaySpecUrl: string | null;
  /** 外挂 spec i18n URL，形如 /assets/gtnh/2.8.4/official/spec/i18n/zh_CN.json */
  displaySpecMessagesUrl: string | null;
}

export interface ModOption {
  modId: string;
  name: string;
}

export interface ModSummary {
  modId: string;
  name: string;
  version: string;
  sourceType: string;
  sourceFileName: string;
  sourceSha256: string;
  enabled: boolean;
}

export interface ApiError {
  code: string;
  message: string;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}
