package edu.stanford.slac.core_work_management.model;

import edu.stanford.slac.core_work_management.model.value.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Model for the custom field used by work and activity types.
 *
 */
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class WATypeCustomField {
    /**
     * The unique identifier for the custom field.
     */
    private String id;
    /**
     * The title of the custom field. This field stores the title or name of the custom field.
     */
    private String name;
    /**
     * The human-readable label of the custom field. This field provides a human-readable label for the custom field.
     */
    private String label;

    /**
     * The group of the custom field. This field provides a group for the custom field.
     * is needed only to help uis to group custom fields together.
     */
    private String group;

    /**
     * The detailed description of the custom field. This field provides a comprehensive description of what the custom field entails.
     */
    private String description;
    /**
     * The type of the custom field.
     */
    private ValueType valueType;
    /**
     * The list of the possible values for the custom field. This field provides a list of the possible values for the custom field.
     */
    private String additionalMappingInfo;
    /**
     * Specify is the custom field is mandatory.
     */
    @Builder.Default
    private String lovFieldReference = UUID.randomUUID().toString();
    /**
     * Specify is the custom field is mandatory.
     */
    @Builder.Default
    private Boolean isMandatory = false;
}
