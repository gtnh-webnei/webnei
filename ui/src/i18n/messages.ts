export interface MessageSchema {
  // ---- Common / shared ----
  common: {
    back: string;
    viewRecipe: string;
    viewUsage: string;
    basicAttributes: string;
    registryName: string;
    unlocalizedName: string;
    chemicalExpression: string;
    mod: string;
    variantId: string;
    renderPreview: string;
    extrasInfo: string;
    asOutput: string;
    asInput: string;
    recipeArrow: string;
    usageArrow: string;
    noExtras: string;
    fluidContainer: string;
    notFound: string;
    loading: string;
    totalCount: string;
    totalSuffix: string;
    showing: string;
    items: string;
    allMod: string;
    tooltip: string;
    nbt: string;
    damageMeta: string;
    amount: string;
    probability: string;
    pickHintSlot: string;
    pickHintCategory: string;
    githubRepository: string;
  };

  // ---- Dataset ----
  dataset: {
    brandName: string;
    leadText: string;
    emptyState: string;
    exportTime: string;
    exporter: string;
    plugins: string;
    startBrowsing: string;
    modList: string;
    selectDataset: string;
    comingSoon: string;
  };

  // ---- Fluid ----
  fluid: {
    searchPlaceholder: string;
    noMatch: string;
    cardPickHint: string;
    fluidId: string;
    state: string;
    gaseous: string;
    liquid: string;
    temperature: string;
    density: string;
    viscosity: string;
    luminosity: string;
    correspondingBlock: string;
    undergroundResources: string;
    notFound: string;
    tag: string;
  };

  // ---- Item ----
  item: {
    searchPlaceholder: string;
    viewCard: string;
    viewCompact: string;
    noMatch: string;
    cardPickHint: string;
    itemId: string;
    maxStackSize: string;
    noIcon: string;
    oreDictEntries: string;
    relatedFluids: string;
    thaumcraftAspects: string;
    primal: string;
    worldGeneration: string;
    notFound: string;
  };

  // ---- Mob ----
  mob: {
    pageTitle: string;
    totalLabel: string;
    searchPlaceholder: string;
    noMatch: string;
    maxHealth: string;
    armor: string;
    fireImmune: string;
    fireImmuneBadge: string;
    leashable: string;
    leashableBadge: string;
    dropNormal: string;
    dropAdditional: string;
    dropRare: string;
    peacefulMode: string;
    allowed: string;
    despawn: string;
    soulVial: string;
    fillable: string;
    notFillable: string;
    infernal: string;
    always: string;
    maybe: string;
    never: string;
    dropTablePrefix: string;
    noDropData: string;
    requirePlayerKill: string;
    notLootBag: string;
  };

  // ---- Quest ----
  quest: {
    pageTitle: string;
    questLineSummary: string;
    searchPlaceholder: string;
    noQuestLines: string;
    taskCount: string;
    taskType: {
      retrieval: string;
      crafting: string;
      hunt: string;
      checkbox: string;
      location: string;
      fluid: string;
      unhandled: string;
    };
    rewardType: {
      item: string;
      choice: string;
      xp: string;
      completeQuest: string;
    };
    nodeTaskCount: string;
    nodeEdgeCount: string;
    fitWindow: string;
    resetZoom: string;
    centerGraph: string;
    zoomIn: string;
    zoomOut: string;
    hoverTooltip: string;
    repeatable: string;
    taskConditions: string;
    huntTarget: string;
    dimension: string;
    rewards: string;
    xpLevel: string;
    xpPoints: string;
    canvasToolbar: string;
  };

  // ---- Category ----
  category: {
    pageTitle: string;
    leadText: string;
    searchPlaceholder: string;
    hideZeroRecipes: string;
    noMatch: string;
    recipeCount: string;
    machineCount: string;
    applicableMachines: string;
    voltage: string;
    allTiers: string;
    categorySearchPlaceholder: string;
    noRecipes: string;
  };

  // ---- Recipe ----
  recipe: {
    recipeId: string;
    sourcePlugin: string;
    sourceRef: string;
    description: string;
    placeholder: string;
    notFound: string;
    noMatch: string;
    shapeless: string;
    gridSlots: string;
    shapedPrefix: string;
    specialItems: string;
    specialInput: string;
    input: string;
    itemCountLabel: string;
    fluidCountLabel: string;
    outputDivider: string;
    output: string;
    specialOutput: string;
    candidates: string;
    tier: string;
  };

  // ---- Recipe Lookup ----
  lookup: {
    detailTab: string;
    recipeTab: string;
    usageTab: string;
    pickHint: string;
    noMatch: string;
    viewTabsLabel: string;
    recipeCountCardTitle: string;
    recipeCountRecipe: string;
    recipeCountUsage: string;
  };

  // ---- Mod ----
  mod: {
    pageTitle: string;
    totalLabel: string;
    searchPlaceholder: string;
    noMatch: string;
    colModId: string;
    colName: string;
    colVersion: string;
    colFile: string;
    colSource: string;
    colEnabled: string;
    colSha256: string;
  };

  // ---- Container ----
  container: {
    asFluidContainer: string;
    totalMappings: string;
    searchPlaceholder: string;
    colFluid: string;
    colFilledContainer: string;
    colCapacity: string;
    colEmptyContainer: string;
  };

  // ---- GT Resource ----
  gtResource: {
    pageTitle: string;
    oreVeins: string;
    smallOres: string;
    undergroundFluids: string;
    bartWorksOres: string;
    searchPlaceholder: string;
    dimensionFilter: string;
    typeFilter: string;
    empty: string;
    dimension: string;
    undergroundFluidTag: string;
    bartWorksTag: string;
    statType: string;
    statChance: string;
    statMinAmount: string;
    statMaxAmount: string;
    statReserve: string;
    statGenerationHeight: string;
    statGenerationWeight: string;
    statGenerationSize: string;
    statGenerationDensity: string;
    statAmountPerChunk: string;
    statWeightShort: string;
    statChanceShort: string;
    statAmountPerChunkShort: string;
    layerPrimary: string;
    layerSecondary: string;
    layerBetween: string;
    layerSporadic: string;
    relatedItem: string;
    chanceDrops: string;
    relatedFluid: string;
    bartWorksVeinGeneration: string;
    bartWorksSmallOreGeneration: string;
    result: string;
    sectionMenuLabel: string;
    selectItemEmpty: string;
  };

  // ---- Theme ----
  theme: {
    light: string;
    dark: string;
    auto: string;
  };

  // ---- Navigation ----
  nav: {
    items: string;
    fluids: string;
    categories: string;
    quests: string;
    mobs: string;
    gt: string;
    mods: string;
  };
}
