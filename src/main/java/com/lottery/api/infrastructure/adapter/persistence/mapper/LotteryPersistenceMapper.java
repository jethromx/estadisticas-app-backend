package com.lottery.api.infrastructure.adapter.persistence.mapper;

import com.lottery.api.domain.model.LotteryDraw;
import com.lottery.api.domain.model.LotteryType;
import com.lottery.api.domain.model.NumberFrequency;
import com.lottery.api.infrastructure.adapter.persistence.entity.LotteryDrawEntity;
import com.lottery.api.infrastructure.adapter.persistence.projection.NumberFrequencyProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper MapStruct entre la entidad JPA y el modelo de dominio.
 *
 * <p>Los números principales se empaquetan/desempaquetan entre la lista del dominio
 * y las columnas individuales de la entidad.</p>
 */
@Mapper(componentModel = "spring")
public interface LotteryPersistenceMapper {

    @Mapping(target = "numbers", source = "entity", qualifiedByName = "entityToNumbers")
    @Mapping(target = "additionalNumber", source = "additionalNumber")
    LotteryDraw toDomain(LotteryDrawEntity entity);

    @Mapping(target = "number1", source = "draw", qualifiedByName = "getNumber1")
    @Mapping(target = "number2", source = "draw", qualifiedByName = "getNumber2")
    @Mapping(target = "number3", source = "draw", qualifiedByName = "getNumber3")
    @Mapping(target = "number4", source = "draw", qualifiedByName = "getNumber4")
    @Mapping(target = "number5", source = "draw", qualifiedByName = "getNumber5")
    @Mapping(target = "number6", source = "draw", qualifiedByName = "getNumber6")
    @Mapping(target = "number7", source = "draw", qualifiedByName = "getNumber7")
    @Mapping(target = "number8", source = "draw", qualifiedByName = "getNumber8")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LotteryDrawEntity toEntity(LotteryDraw draw);

    @Mapping(target = "number",        source = "number")
    @Mapping(target = "frequency",     source = "frequency")
    @Mapping(target = "lastDrawnDate", source = "lastDrawnDate")
    @Mapping(target = "lastDrawNumber", source = "lastDrawNumber")
    @Mapping(target = "percentage",    ignore = true)
    NumberFrequency projectionToDomain(NumberFrequencyProjection projection);

    // ---------- helpers -------------------------------------------------------

    @Named("entityToNumbers")
    default List<Integer> entityToNumbers(LotteryDrawEntity e) {
        List<Integer> list = new ArrayList<>();
        addIfNotNull(list, e.getNumber1());
        addIfNotNull(list, e.getNumber2());
        addIfNotNull(list, e.getNumber3());
        addIfNotNull(list, e.getNumber4());
        addIfNotNull(list, e.getNumber5());
        addIfNotNull(list, e.getNumber6());
        addIfNotNull(list, e.getNumber7());
        addIfNotNull(list, e.getNumber8());
        return list;
    }

    @Named("getNumber1")
    default Integer getNumber1(LotteryDraw d) { return getNumberAt(d, 0); }
    @Named("getNumber2")
    default Integer getNumber2(LotteryDraw d) { return getNumberAt(d, 1); }
    @Named("getNumber3")
    default Integer getNumber3(LotteryDraw d) { return getNumberAt(d, 2); }
    @Named("getNumber4")
    default Integer getNumber4(LotteryDraw d) { return getNumberAt(d, 3); }
    @Named("getNumber5")
    default Integer getNumber5(LotteryDraw d) { return getNumberAt(d, 4); }
    @Named("getNumber6")
    default Integer getNumber6(LotteryDraw d) { return getNumberAt(d, 5); }
    @Named("getNumber7")
    default Integer getNumber7(LotteryDraw d) { return getNumberAt(d, 6); }
    @Named("getNumber8")
    default Integer getNumber8(LotteryDraw d) { return getNumberAt(d, 7); }

    private Integer getNumberAt(LotteryDraw draw, int index) {
        if (draw.getNumbers() == null || index >= draw.getNumbers().size()) return null;
        return draw.getNumbers().get(index);
    }

    private void addIfNotNull(List<Integer> list, Integer value) {
        if (value != null) list.add(value);
    }

    default List<LotteryDraw> toDomainList(List<LotteryDrawEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }

    /**
     * Calcula el porcentaje de cada frecuencia respecto al total de apariciones del conjunto.
     *
     * @param projections lista de proyecciones raw
     * @param lotteryType tipo de juego (para validar el rango)
     * @return lista con porcentajes calculados
     */
    default List<NumberFrequency> toNumberFrequencyList(
            List<NumberFrequencyProjection> projections, LotteryType lotteryType) {

        long total = projections.stream().mapToLong(NumberFrequencyProjection::getFrequency).sum();
        return projections.stream().map(p -> {
            NumberFrequency nf = projectionToDomain(p);
            nf.setPercentage(total > 0 ? (nf.getFrequency() * 100.0) / total : 0.0);
            return nf;
        }).toList();
    }
}
