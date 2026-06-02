package moe.takochan.webnei.mob;

import java.util.List;

import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetService;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.mob.dto.MobDetail;
import moe.takochan.webnei.mob.dto.MobSummary;

import org.springframework.stereotype.Service;

@Service
public class MobService {

    private final DatasetService datasetService;
    private final MobDao mobDao;

    public MobService(DatasetService datasetService, MobDao mobDao) {
        this.datasetService = datasetService;
        this.mobDao = mobDao;
    }

    public PageResponse<MobSummary> listMobs(
            String datasetId, String q, String modId, PageRequest page) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return mobDao.listMobs(dataset, q, modId, page);
    }

    public List<ModOptionDto> listMods(String datasetId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return mobDao.listMods(dataset);
    }

    public MobDetail mobDetail(String datasetId, String mobVariantId) {
        DatasetSummary dataset = datasetService.requireById(datasetId);
        return mobDao.findMob(dataset, mobVariantId)
                .orElseThrow(() -> new NotFoundException("Mob not found: " + mobVariantId));
    }
}
