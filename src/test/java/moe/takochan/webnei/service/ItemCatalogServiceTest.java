package moe.takochan.webnei.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import moe.takochan.webnei.dto.common.PageResponse;
import moe.takochan.webnei.dto.item.ItemListEntry;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.entity.ItemBrowserRow;
import moe.takochan.webnei.mapper.DatasetMapper;
import moe.takochan.webnei.mapper.ItemBrowserMapper;
import moe.takochan.webnei.search.SearchQueryParser;

@ExtendWith(MockitoExtension.class)
class ItemCatalogServiceTest {

    private static final String DATASET_ID = "dataset-id";

    @Mock
    private DatasetMapper datasetMapper;
    @Mock
    private ItemBrowserMapper itemBrowserMapper;
    @Mock
    private SearchQueryParser searchQueryParser;
    @Mock
    private AssetUrlService assetUrlService;

    private ItemCatalogService service;
    private DatasetEntity dataset;

    @BeforeEach
    void setUp() {
        service = new ItemCatalogService(
                datasetMapper,
                itemBrowserMapper,
                searchQueryParser,
                assetUrlService);
        dataset = new DatasetEntity();
        when(datasetMapper.selectById(DATASET_ID)).thenReturn(dataset);
        when(searchQueryParser.parse(any())).thenReturn(List.of());
    }

    @Test
    void mapsListCardFieldsAndPagination() {
        ItemBrowserRow row = new ItemBrowserRow();
        row.setItemVariantId("variant-id");
        row.setDisplayName("Item");
        row.setModId("mod-id");
        row.setModName("Mod Name");
        row.setRegistryName("mod:item");
        row.setListIndex(7);
        row.setIconPath("icons/item.png");
        row.setIconWidth(16);
        row.setIconHeight(16);
        row.setIconMetadataJson("{\"animated\":false}");
        stubPage(List.of(row), 21);
        when(assetUrlService.assetUrl(dataset, "icons/item.png")).thenReturn("/assets/icons/item.png");

        PageResponse<ItemListEntry> response = service.list(DATASET_ID, null, 2, 40);

        assertThat(response.getPage()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(40);
        assertThat(response.getTotal()).isEqualTo(21);
        assertThat(response.getItems()).singleElement().satisfies(entry -> {
            assertThat(entry.getId()).isEqualTo("variant-id");
            assertThat(entry.getDisplayName()).isEqualTo("Item");
            assertThat(entry.getModId()).isEqualTo("mod-id");
            assertThat(entry.getModName()).isEqualTo("Mod Name");
            assertThat(entry.getRegistryName()).isEqualTo("mod:item");
            assertThat(entry.getIcon().getUrl()).isEqualTo("/assets/icons/item.png");
            assertThat(entry.getIcon().getWidth()).isEqualTo(16);
            assertThat(entry.getIcon().getHeight()).isEqualTo(16);
            assertThat(entry.getIcon().getMetadataJson()).isEqualTo("{\"animated\":false}");
        });
    }

    @Test
    void returnsEmptyPage() {
        stubPage(List.of(), 0);

        PageResponse<ItemListEntry> response = service.list(DATASET_ID, null, -1, 0);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(60);
        assertThat(response.getTotal()).isZero();
    }

    private void stubPage(List<ItemBrowserRow> records, long total) {
        Page<ItemBrowserRow> page = new Page<>(1, 60);
        page.setRecords(records);
        page.setTotal(total);
        when(itemBrowserMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);
    }
}
