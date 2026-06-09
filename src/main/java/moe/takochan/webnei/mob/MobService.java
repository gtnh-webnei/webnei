package moe.takochan.webnei.mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.EntityRefService;
import moe.takochan.webnei.common.ItemRef;
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
    private final MobSearchDocumentRepository searchRepo;
    private final MobModOptionRepository modOptionRepo;
    private final MobInfoRepository mobInfoRepo;
    private final MobDropBrowserRepository mobDropRepo;
    private final AssetUrlBuilder assetUrlBuilder;
    private final EntityRefService entityRefService;

    public MobService(MobVariantRepository mobRepo,
                      MobSearchDocumentRepository searchRepo,
                      MobModOptionRepository modOptionRepo,
                      MobInfoRepository mobInfoRepo,
                      MobDropBrowserRepository mobDropRepo,
                      AssetUrlBuilder assetUrlBuilder,
                      EntityRefService entityRefService) {
        this.mobRepo = mobRepo;
        this.searchRepo = searchRepo;
        this.modOptionRepo = modOptionRepo;
        this.mobInfoRepo = mobInfoRepo;
        this.mobDropRepo = mobDropRepo;
        this.assetUrlBuilder = assetUrlBuilder;
        this.entityRefService = entityRefService;
    }

    public PageResponse<MobSummary> listMobs(
            DatasetSummary dataset, String q, String modId, PageRequest page) {
        if (q != null && !q.isBlank()) {
            return searchMobs(dataset, q, modId, page);
        }

        String datasetId = dataset.datasetId();
        Specification<MobVariantBrowserEntity> spec = hasDatasetId(datasetId);
        if (modId != null && !modId.isBlank()) {
            spec = spec.and(modIdEq(modId));
        }

        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize,
                Sort.by("displayName").ascending()
                        .and(Sort.by("mobVariantId").ascending()));

        Page<MobVariantBrowserEntity> result = mobRepo.findAll(spec, pageable);
        List<MobSummary> items = result.stream()
                .map(e -> toSummary(e, dataset))
                .toList();
        return new PageResponse<>(items, pageIndex, pageSize, result.getTotalElements());
    }

    private PageResponse<MobSummary> searchMobs(
            DatasetSummary dataset, String q, String modId, PageRequest page) {
        String datasetId = dataset.datasetId();
        int pageIndex = page != null ? page.page() : 0;
        int pageSize = page != null ? page.size() : 100;
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageIndex, pageSize,
                Sort.by("displayName").ascending()
                        .and(Sort.by("mobVariantId").ascending()));
        Page<MobSearchDocumentEntity> result = searchRepo.findAll(searchSpec(datasetId, q, modId), pageable);
        List<String> ids = result.stream()
                .map(MobSearchDocumentEntity::getMobVariantId)
                .toList();
        Map<String, MobVariantBrowserEntity> rows = mobRepo.findAllById(ids.stream()
                        .map(id -> new MobVariantBrowserEntity.MobVariantId(datasetId, id))
                        .toList())
                .stream()
                .collect(Collectors.toMap(MobVariantBrowserEntity::getMobVariantId, Function.identity()));
        List<MobSummary> items = ids.stream()
                .map(rows::get)
                .filter(e -> e != null)
                .map(e -> toSummary(e, dataset))
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
        List<MobDropBrowserEntity> dropRows = mobDropRepo.findByDatasetIdAndMobVariantId(
                datasetId, mobVariantId,
                Sort.by("probability").descending().and(Sort.by("listIndex").ascending()));
        Map<String, ItemRef> dropItems = entityRefService.itemRefs(dataset, dropRows.stream()
                .map(MobDropBrowserEntity::getItemVariantId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList());
        List<MobDropRow> drops = dropRows.stream()
                .map(e -> toDropRow(e, dropItems))
                .toList();
        return new MobDetail(toSummary(mob, dataset), info, drops);
    }

    private MobSummary toSummary(MobVariantBrowserEntity e, DatasetSummary dataset) {
        return new MobSummary(
                e.getMobVariantId(), e.getMobId(), e.getModId(), e.getModName(),
                e.getEntityName(), e.getDisplayName(),
                e.getWidth(), e.getHeight(), e.getMaxHealth(), e.getArmor(),
                e.isImmuneToFire(), e.isLeashable(),
                assetUrlBuilder.build(dataset, e.getAssetPath(), null));
    }

    private MobDetail.MobInfo toInfo(MobInfoEntity e) {
        return new MobDetail.MobInfo(
                e.isAllowedInPeaceful(), e.isSoulVialUsable(),
                e.isAllowedInfernal(), e.isAlwaysInfernal());
    }

    private MobDropRow toDropRow(MobDropBrowserEntity e, Map<String, ItemRef> dropItems) {
        ItemRef item = dropItems.get(e.getItemVariantId());
        return new MobDropRow(
                e.getDropType(), e.getListIndex(), e.getItemVariantId(),
                item == null ? null : item.displayName(),
                item == null ? null : item.assetUrl(),
                e.getStackSize(), e.getProbability(), e.isLootable(), e.isPlayerOnly());
    }

    private static Specification<MobVariantBrowserEntity> hasDatasetId(String datasetId) {
        return (root, query, cb) -> cb.equal(root.get("datasetId"), datasetId);
    }

    private static Specification<MobVariantBrowserEntity> modIdEq(String modId) {
        return (root, query, cb) -> cb.equal(root.get("modId"), modId);
    }

    private static Specification<MobSearchDocumentEntity> searchSpec(String datasetId, String q, String modId) {
        return (root, cq, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("datasetId"), datasetId));
            predicates.add(cb.like(root.get("searchText"), "%" + q.trim().toLowerCase() + "%"));
            if (modId != null && !modId.isBlank()) {
                predicates.add(cb.equal(root.get("modId"), modId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
