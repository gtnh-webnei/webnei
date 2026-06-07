package moe.takochan.webnei.mob;

import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.NotFoundException;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.mob.dto.MobDetail;
import moe.takochan.webnei.mob.dto.MobDropRow;
import moe.takochan.webnei.mob.dto.MobSummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MobService {

    private final MobVariantRepository mobRepo;
    private final MobModOptionRepository modOptionRepo;
    private final MobInfoRepository mobInfoRepo;
    private final MobDropBrowserRepository mobDropRepo;
    private final AssetUrlBuilder assetUrlBuilder;

    public MobService(MobVariantRepository mobRepo,
                      MobModOptionRepository modOptionRepo,
                      MobInfoRepository mobInfoRepo,
                      MobDropBrowserRepository mobDropRepo,
                      AssetUrlBuilder assetUrlBuilder) {
        this.mobRepo = mobRepo;
        this.modOptionRepo = modOptionRepo;
        this.mobInfoRepo = mobInfoRepo;
        this.mobDropRepo = mobDropRepo;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public PageResponse<MobSummary> listMobs(
            DatasetSummary dataset, String q, String modId, PageRequest page) {
        String datasetId = dataset.datasetId();
        Specification<MobVariantBrowserEntity> spec = hasDatasetId(datasetId);
        if (modId != null && !modId.isBlank()) {
            spec = spec.and(modIdEq(modId));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(textSearch(q));
        }

        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize,
                Sort.by("displayName").ascending()
                        .and(Sort.by("mobVariantId").ascending()));

        Page<MobVariantBrowserEntity> result = mobRepo.findAll(spec, pageable);
        Map<String, String> modNames = loadModNames(datasetId);
        List<MobSummary> items = result.stream()
                .map(e -> toSummary(e, dataset, modNames))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    public List<ModOptionDto> listMods(DatasetSummary dataset) {
        return modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(dataset.datasetId())
                .stream()
                .map(e -> new ModOptionDto(e.getModId(), e.getName()))
                .toList();
    }

    public MobDetail mobDetail(DatasetSummary dataset, String mobVariantId) {
        String datasetId = dataset.datasetId();
        MobVariantBrowserEntity mob = mobRepo.findById(new MobVariantBrowserEntity.MobVariantId(datasetId, mobVariantId))
                .orElseThrow(() -> new NotFoundException("Mob not found: " + mobVariantId));
        MobDetail.MobInfo info = mobInfoRepo.findById(new MobInfoEntity.MobInfoId(datasetId, mobVariantId))
                .map(this::toInfo)
                .orElse(new MobDetail.MobInfo(false, false, false, false));
        List<MobDropRow> drops = mobDropRepo.findByDatasetIdAndMobVariantId(
                        datasetId, mobVariantId,
                        Sort.by("probability").descending().and(Sort.by("listIndex").ascending()))
                .stream()
                .map(e -> toDropRow(e, dataset))
                .toList();
        return new MobDetail(toSummary(mob, dataset, loadModNames(datasetId)), info, drops);
    }

    private MobSummary toSummary(MobVariantBrowserEntity e, DatasetSummary dataset, Map<String, String> modNames) {
        return new MobSummary(
                e.getMobVariantId(), e.getMobId(), e.getModId(), modNames.getOrDefault(e.getModId(), e.getModId()),
                e.getEntityName(), e.getDisplayName(),
                e.getWidth(), e.getHeight(), e.getMaxHealth(), e.getArmor(),
                e.isImmuneToFire(), e.isLeashable(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), null));
    }

    private Map<String, String> loadModNames(String datasetId) {
        Map<String, String> names = new java.util.HashMap<>();
        modOptionRepo.findByDatasetIdOrderByNameAscModIdAsc(datasetId)
                .forEach(e -> names.put(e.getModId(), e.getName()));
        return names;
    }

    private MobDetail.MobInfo toInfo(MobInfoEntity e) {
        return new MobDetail.MobInfo(
                e.isAllowedInPeaceful(), e.isSoulVialUsable(),
                e.isAllowedInfernal(), e.isAlwaysInfernal());
    }

    private MobDropRow toDropRow(MobDropBrowserEntity e, DatasetSummary dataset) {
        return new MobDropRow(
                e.getDropType(), e.getListIndex(), e.getItemVariantId(),
                e.getDisplayName(), assetUrlBuilder.build(dataset, e.getAssetPath(), e.getAssetSha256()),
                e.getStackSize(), e.getProbability(), e.isLootable(), e.isPlayerOnly());
    }

    private static Specification<MobVariantBrowserEntity> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<MobVariantBrowserEntity> modIdEq(String modId) {
        return (root, query, cb) -> cb.equal(root.get("modId"), modId);
    }

    private static Specification<MobVariantBrowserEntity> textSearch(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> {
            Predicate[] conditions = {
                    cb.like(cb.lower(root.get("displayName")), pattern),
                    cb.like(cb.lower(root.get("entityName")), pattern)
            };
            return cb.or(conditions);
        };
    }
}
