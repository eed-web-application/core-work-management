/*
 * -----------------------------------------------------------------------------
 * Title      : LOVServiceTest
 * ----------------------------------------------------------------------------
 * File       : LOVServiceTest.java
 * Author     : Claudio Bisegni, bisegni@slac.stanford.edu
 * ----------------------------------------------------------------------------
 * This file is part of core-work-management. It is subject to
 * the license terms in the LICENSE.txt file found in the top-level directory
 * of this distribution and at:
 * <a href="https://confluence.slac.stanford.edu/display/ppareg/LICENSE.html"/>.
 * No part of core-work-management, including this file, may be
 * copied, modified, propagated, or distributed except according to the terms
 *  contained in the LICENSE.txt file.
 * ----------------------------------------------------------------------------
 */

package edu.stanford.slac.core_work_management.service;

import com.google.common.collect.ImmutableSet;
import edu.stanford.slac.core_work_management.api.v1.dto.*;
import edu.stanford.slac.core_work_management.exception.LOVFieldReferenceNotFound;
import edu.stanford.slac.core_work_management.model.*;
import edu.stanford.slac.core_work_management.repository.LOVElementRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LOVServiceTest {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private LOVService lovService;
    @Autowired
    private LOVElementRepository lovElementRepository;
    @Autowired
    private HelperService helperService;
    @Autowired
    private DomainService domainService;
    @Autowired
    private WorkService workService;
    @Autowired
    private ShopGroupService shopGroupService;
    @Autowired
    private LocationService locationService;

    private DomainDTO domainDTO;
    private List<String> workIds;
    private WorkTypeDTO fullWorkType;
    private String shopGroupId;
    private String locationId;

    @BeforeAll
    public void setup() {
        mongoTemplate.remove(new Query(), Domain.class);
        mongoTemplate.remove(new Query(), ShopGroup.class);
        mongoTemplate.remove(new Query(), Location.class);
        mongoTemplate.remove(new Query(), Work.class);
        mongoTemplate.remove(new Query(), WorkType.class);
        // create domain
        domainDTO = assertDoesNotThrow(
                () -> domainService.createNewAndGet(
                        NewDomainDTO
                                .builder()
                                .name("Domain 1")
                                .description("Domain 1 description")
                                .workflowImplementations(
                                        ImmutableSet.of("DummyParentWorkflow")
                                )
                                .build()
                )
        );
        assertThat(domainDTO).isNotNull();

        // create test work
        workIds = helperService.ensureWorkAndActivitiesTypes(
                domainDTO.id(),
                NewWorkTypeDTO
                        .builder()
                        .title("Update the documentation")
                        .description("Update the documentation description")
                        .validatorName("validation/DummyParentValidation.groovy")
                        .workflowId(domainDTO.workflows().stream().findFirst().get().id())
                        .customFields(
                                of(
                                        WATypeCustomFieldDTO.builder().name("field1").description("field1 description").valueType(ValueTypeDTO.String).build(),
                                        WATypeCustomFieldDTO.builder().name("field2").description("value2 description").valueType(ValueTypeDTO.String).build()
                                )
                        )
                        .build(),
                of()
        );
        assertThat(workIds).hasSize(1);

        // find work type by id
        fullWorkType = assertDoesNotThrow(
                () -> domainService.findWorkTypeById(domainDTO.id(), workIds.get(0))
        );

        shopGroupId =
                assertDoesNotThrow(
                        () -> shopGroupService.createNew(
                                domainDTO.id(),
                                NewShopGroupDTO.builder()
                                        .name("shop1")
                                        .description("shop1 user[2-3]")
                                        .users(
                                                ImmutableSet.of(
                                                        ShopGroupUserInputDTO.builder()
                                                                .userId("user2@slac.stanford.edu")
                                                                .build(),
                                                        ShopGroupUserInputDTO.builder()
                                                                .userId("user3@slac.stanford.edu")
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                );
        AssertionsForClassTypes.assertThat(shopGroupId).isNotEmpty();

        locationId =
                assertDoesNotThrow(
                        () -> locationService.createNew(
                                domainDTO.id(),
                                NewLocationDTO.builder()
                                        .name("SLAC")
                                        .description("SLAC National Accelerator Laboratory")
                                        .locationManagerUserId("user1@slac.stanford.edu")
                                        .build()
                        )
                );
        AssertionsForClassTypes.assertThat(locationId).isNotEmpty();
    }

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), LOVElement.class);
    }

    @Test
    public void testCreateWithOnlyGroupName() {
        // add lov for static field
        var lovIds = assertDoesNotThrow(
                () -> lovService.createNew(
                        "group-1",
                        of(
                                NewLOVElementDTO.builder().value("group-1 value1").description("group-1 value1 description").build(),
                                NewLOVElementDTO.builder().value("group-1 value2").description("group-1 value2 description").build()
                        )
                )
        );
        var listOfAllLOVGroup1 = assertDoesNotThrow(
                () -> lovService.findAllByGroupName("group-1")
        );
        assertThat(listOfAllLOVGroup1).hasSize(2);
        assertThat(listOfAllLOVGroup1).extracting(LOVElementDTO::value).contains("group-1 value1", "group-1 value2");

        lovIds = assertDoesNotThrow(
                () -> lovService.createNew(
                        "group-2",
                        of(
                                NewLOVElementDTO.builder().value("group-2 value1").description("group-2 value1 description").build(),
                                NewLOVElementDTO.builder().value("group-2 value2").description("group-2 value2 description").build()
                        )
                )
        );
        var listOfAllLOVGroup2 = assertDoesNotThrow(
                () -> lovService.findAllByGroupName("group-2")
        );
        assertThat(listOfAllLOVGroup2).hasSize(2);
        assertThat(listOfAllLOVGroup2).extracting(LOVElementDTO::value).contains("group-2 value1", "group-2 value2");
    }

    @Test
    public void testAddAndRemoveFieldReferenceToGroupName() {
        var lovIds = assertDoesNotThrow(
                () -> lovService.createNew(
                        "group-1",
                        of(
                                NewLOVElementDTO.builder().value("group-1 value1").description("group-1 value1 description").build(),
                                NewLOVElementDTO.builder().value("group-1 value2").description("group-1 value2 description").build()
                        )
                )
        );
        assertDoesNotThrow(
                () -> lovService.addFieldReferenceToGroupName(
                        "group-1",
                        of("field1", "field2")
                )
        );
        // check if the field reference has been added
        var elementList = lovElementRepository.findByGroupNameIs("group-1");
        elementList.forEach(
                element -> {
                    assertThat(element.getFieldReference()).contains("field1", "field2");
                }
        );

        assertDoesNotThrow(
                () -> lovService.removeFieldReferenceFromGroupName(
                        "group-1",
                        of("field2")
                )
        );
        elementList.forEach(
                element -> {
                    assertThat(element.getFieldReference()).contains("field1");
                }
        );
    }

    @Test
    public void createNewLOVElementForDomainAndDynamicField() {
        var lovIds = assertDoesNotThrow(
                () -> lovService.createNew(
                        "field1_group",
                        of(
                                NewLOVElementDTO.builder().value("field1 value1").description("field1 value1 description").build(),
                                NewLOVElementDTO.builder().value("field1 value2").description("field1 value2 description").build()
                        )
                )
        );
        // add lov for dynamic field
        assertDoesNotThrow(
                () -> lovService.associateDomainFieldToGroupName(
                        LOVDomainTypeDTO.Work,
                        domainDTO.id(),
                        workIds.get(0),
                        "field1",
                        "field1_group"
                )
        );
        var listOfAllLOV = assertDoesNotThrow(
                () -> lovService.findAllByDomainAndFieldName(LOVDomainTypeDTO.Work, domainDTO.id(),  workIds.get(0), "field1")
        );
        assertThat(listOfAllLOV).hasSize(2);
        assertThat(listOfAllLOV).extracting(LOVElementDTO::value).contains("field1 value1", "field1 value2");
    }

    @Test
    public void createNewLOVElementForDomainFailOnWrongFieldName() {
        // add lov for dynamic field
        var fieldNotFound = assertThrows(
                LOVFieldReferenceNotFound.class,
                () -> lovService.associateDomainFieldToGroupName(
                        LOVDomainTypeDTO.Work,
                        domainDTO.id(),
                        workIds.get(0),
                        "wrong field",
                        "field1_group"
                )
        );
        assertThat(fieldNotFound.getErrorCode()).isEqualTo(-1);
    }
}
