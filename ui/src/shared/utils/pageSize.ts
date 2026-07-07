const MAX_PAGE_SIZE = 120;

/**
 * 按窗口宽度分档给出每页条数，打开时取一次即固定。
 * 各档数值按该宽度下典型屏幕大致铺满一屏（列×行）估算，避免逐像素测量带来的时序与抖动问题。
 * 不超过后端每页上限（120）。
 */
export function pageSizeForViewport(width = window.innerWidth): number {
  if (width < 620) return 20; // 1
  if (width < 955) return 30; // 2
  if (width < 1258) return 48; // 3
  if (width < 1546) return 60; // 4
  if (width < 1834) return 80; // 5
  if (width < 2122) return 90; // 6
  if (width < 2410) return 105; // 7
  return MAX_PAGE_SIZE;
}
