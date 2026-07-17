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
import moe.takochan.webnei.dto.fluid.FluidListEntry;
import moe.takochan.webnei.entity.DatasetEntity;
import moe.takochan.webnei.entity.FluidBrowserRow;
import moe.takochan.webnei.mapper.DatasetMapper;
import moe.takochan.webnei.mapper.FluidBrowserMapper;
import moe.takochan.webnei.search.SearchQueryParser;

@ExtendWith(MockitoExtension.class)
class FluidCatalogServiceTest {

    private static final String DATASET_ID = "dataset-id";

    @Mock
    private DatasetMapper datasetMapper;
    @Mock
    private FluidBrowserMapper fluidBrowserMapper;
    @Mock
    private SearchQueryParser searchQueryParser;
    @Mock
    private AssetUrlService assetUrlService;

    private FluidCatalogService service;
    private DatasetEntity dataset;

    @BeforeEach
    void setUp() {
        service = new FluidCatalogService(
                datasetMapper,
                fluidBrowserMapper,
                searchQueryParser,
                assetUrlService);
        dataset = new DatasetEntity();
        when(datasetMapper.selectById(DATASET_ID)).thenReturn(dataset);
        when(searchQueryParser.parse(any())).thenReturn(List.of());
    }

    @Test
    void mapsListCardFieldsAndPagination() {
        FluidBrowserRow row = new FluidBrowserRow();
        row.setFluidId("fluid-id");
        row.setDisplayName("Fluid");
        row.setModId("mod-id");
        row.setModName("Mod Name");
        row.setRegistryName("mod:fluid");
        row.setIconPath("icons/fluid.png");
        row.setIconWidth(16);
        row.setIconHeight(32);
        row.setIconMetadataJson("{\"frames\":2}");
        stubPage(List.of(row), 13);
        when(assetUrlService.assetUrl(dataset, "icons/fluid.png")).thenReturn("/assets/icons/fluid.png");

        PageResponse<FluidListEntry> response = service.list(DATASET_ID, null, 1, 30);

        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(30);
        assertThat(response.getTotal()).isEqualTo(13);
        assertThat(response.getItems()).singleElement().satisfies(entry -> {
            assertThat(entry.getId()).isEqualTo("fluid-id");
            assertThat(entry.getDisplayName()).isEqualTo("Fluid");
            assertThat(entry.getModId()).isEqualTo("mod-id");
            assertThat(entry.getModName()).isEqualTo("Mod Name");
            assertThat(entry.getRegistryName()).isEqualTo("mod:fluid");
            assertThat(entry.getIcon().getUrl()).isEqualTo("/assets/icons/fluid.png");
            assertThat(entry.getIcon().getWidth()).isEqualTo(16);
            assertThat(entry.getIcon().getHeight()).isEqualTo(32);
            assertThat(entry.getIcon().getMetadataJson()).isEqualTo("{\"frames\":2}");
        });
    }

    @Test
    void returnsEmptyPage() {
        stubPage(List.of(), 0);

        PageResponse<FluidListEntry> response = service.list(DATASET_ID, null, -1, 0);

        assertThat(response.getItems()).isEmpty();
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(60);
        assertThat(response.getTotal()).isZero();
    }

    private void stubPage(List<FluidBrowserRow> records, long total) {
        Page<FluidBrowserRow> page = new Page<>(1, 60);
        page.setRecords(records);
        page.setTotal(total);
        when(fluidBrowserMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);
    }
}
