export interface QuestLineSummary {
  questLineId: string
  name: string
  description: string
  visibility: string
  iconItemVariantId: string
  iconAssetUrl: string | null
  questCount: number
}

export interface QuestNode {
  questId: string
  name: string
  description: string
  visibility: string
  repeatTime: number
  iconItemVariantId: string
  iconAssetUrl: string | null
  posX: number
  posY: number
  sizeX: number
  sizeY: number
}

export interface QuestEdge {
  questId: string
  requiredQuestId: string
}

export interface QuestLineDetail {
  line: QuestLineSummary
  nodes: QuestNode[]
  edges: QuestEdge[]
}

export interface QuestTaskItemEntry {
  itemVariantId: string | null
  fluidVariantId: string | null
  amount: number
  displayName: string | null
  modId: string | null
  assetUrl: string | null
}

export interface QuestTaskItemGroup {
  listIndex: number
  groupId: string
  entries: QuestTaskItemEntry[]
}

export interface QuestTaskDetail {
  taskId: string
  listIndex: number
  name: string
  taskType: string
  consume: boolean
  numberRequired: number
  mobVariantId: string | null
  dimensionName: string | null
  itemGroups: QuestTaskItemGroup[]
}

export interface QuestRewardDetail {
  rewardId: string
  listIndex: number
  name: string
  rewardType: string
  command: string
  xp: number
  levels: boolean
  completeQuestId: string | null
  itemGroups: QuestTaskItemGroup[]
}

export interface QuestDetail {
  node: QuestNode
  tasks: QuestTaskDetail[]
  rewards: QuestRewardDetail[]
  questLogic: string
  taskLogic: string
}
