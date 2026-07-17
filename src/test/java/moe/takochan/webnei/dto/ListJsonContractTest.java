package moe.takochan.webnei.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import moe.takochan.webnei.dto.fluid.FluidListEntry;
import moe.takochan.webnei.dto.item.ItemListEntry;

class ListJsonContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesItemWithExactlyListCardFields() throws JacksonException {
        ItemListEntry entry = new ItemListEntry(
                "entry-id",
                "Test Item",
                "testmod",
                "Test Mod",
                "testmod:test_item",
                null);

        JsonNode json = serialize(entry);

        assertThat(json.propertyNames()).containsExactlyInAnyOrder(
                "id",
                "displayName",
                "modId",
                "modName",
                "registryName",
                "icon");
        assertThat(json.has("itemVariantId")).isFalse();
        assertThat(json.has("itemId")).isFalse();
        assertThat(json.has("damage")).isFalse();
        assertThat(json.has("listIndex")).isFalse();
        assertThat(json.has("tooltipSnapshots")).isFalse();
        assertThat(json.has("chemicalExpression")).isFalse();
    }

    @Test
    void serializesFluidWithExactlyListCardFields() throws JacksonException {
        FluidListEntry entry = new FluidListEntry(
                "fluid-id",
                "Test Fluid",
                "testmod",
                "Test Mod",
                "testmod:test_fluid",
                null);

        JsonNode json = serialize(entry);

        assertThat(json.propertyNames()).containsExactlyInAnyOrder(
                "id",
                "displayName",
                "modId",
                "modName",
                "registryName",
                "icon");
        assertThat(json.has("fluidId")).isFalse();
        assertThat(json.has("chemicalExpression")).isFalse();
        assertThat(json.has("temperature")).isFalse();
        assertThat(json.has("gaseous")).isFalse();
    }

    private JsonNode serialize(Object value) throws JacksonException {
        return objectMapper.readTree(objectMapper.writeValueAsString(value));
    }
}
