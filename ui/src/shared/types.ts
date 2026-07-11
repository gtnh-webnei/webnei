export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  total: number
}

/** 数据集 + 搜索词 + 分页 的通用列表查询参数。 */
export interface SearchListParams {
  datasetId: string
  q?: string
  page: number
  size: number
}

export interface ApiError {
  code: string
  message: string
}

export interface IconAsset {
  url: string
  width: number | null
  height: number | null
  metadataJson: string | null
}

/** 列表卡片渲染所需的通用条目形状；各领域的列表项类型据此扩展。 */
export interface EntryBase {
  id: string
  displayName: string
  modId: string
  modName: string | null
  registryName: string
  icon: IconAsset | null
}

/** 列表条目的图标渲染类别：item / recipeCategory / aspect 静态图走 <img>，fluid 走精灵表动画分支。 */
export type EntryKind = 'item' | 'fluid' | 'recipeCategory' | 'aspect'
