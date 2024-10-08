package edu.stanford.slac.core_work_management.service;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.AuthorizationTypeDTO;
import edu.stanford.slac.ad.eed.baselib.api.v1.dto.PersonDTO;
import edu.stanford.slac.ad.eed.baselib.exception.PersonNotFound;
import edu.stanford.slac.ad.eed.baselib.model.Authorization;
import edu.stanford.slac.ad.eed.baselib.service.AuthService;
import edu.stanford.slac.core_work_management.api.v1.dto.*;
import edu.stanford.slac.core_work_management.model.Domain;
import edu.stanford.slac.core_work_management.model.ShopGroup;
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

import static com.google.common.collect.ImmutableSet.of;
import static edu.stanford.slac.core_work_management.config.AuthorizationStringConfig.SHOP_GROUP_AUTHORIZATION_TEMPLATE;
import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * -----------------------------------------------------------------------------
 * Title      : ShopGroupService
 * ----------------------------------------------------------------------------
 * File       : null.java
 * Author     : Claudio Bisegni, bisegni@slac.stanford.edu
 * Created    : 1/29/24
 * ----------------------------------------------------------------------------
 * This file is part of core-work-management. It is subject to
 * the license terms in the LICENSE.txt file found in the top-level directory
 * of this distribution and at:
 * <a href="https://confluence.slac.stanford.edu/display/ppareg/LICENSE.html"/>.
 * No part of core-work-management, including this file, may be
 * copied, modified, propagated, or distributed except according to the terms
 * contained in the LICENSE.txt file.
 * ----------------------------------------------------------------------------
 **/


@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ShopGroupServiceTest {
    @Autowired
    private DomainService domainService;
    @Autowired
    private ShopGroupService shopGroupService;
    @Autowired
    private AuthService authService;
    @Autowired
    private MongoTemplate mongoTemplate;

    private DomainDTO domainDTO = null;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), Domain.class);
        mongoTemplate.remove(new Query(), Authorization.class);
        mongoTemplate.remove(new Query(), ShopGroup.class);

        domainDTO = assertDoesNotThrow(
                () -> domainService.createNewAndGet(
                        NewDomainDTO.builder()
                                .name("domain1")
                                .description("domain1")
                                .workflowImplementations(of("DummyParentWorkflow"))
                                .build()
                )
        );
    }

    @Test
    public void createNewShopGroupOK() {
        assertDoesNotThrow(
                () -> shopGroupService.createNew(
                        domainDTO.id(),
                        NewShopGroupDTO.builder()
                                .name("shop1")
                                .description("shop1 user[2-3]")
                                .users(
                                        of(
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
    }

    @Test
    public void fetchShopGroupOK() {
        var newShopGroupId = assertDoesNotThrow(
                () -> shopGroupService.createNew(
                        domainDTO.id(),
                        NewShopGroupDTO.builder()
                                .name("shop1")
                                .description("shop1 user[2-3]")
                                .users(
                                        of(
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

        var foundShopGroup = assertDoesNotThrow(
                () -> shopGroupService.findByDomainIdAndId(domainDTO.id(), newShopGroupId)
        );

        assertThat(foundShopGroup).isNotNull();
        assertThat(foundShopGroup.id()).isEqualTo(newShopGroupId);
        assertThat(foundShopGroup.name()).isEqualTo("shop1");
        assertThat(foundShopGroup.description()).isEqualTo("shop1 user[2-3]");
        assertThat(foundShopGroup.users())
                .hasSize(2)
                .extracting(ShopGroupUserDTO::user)
                .extracting(PersonDTO::mail)
                .contains("user2@slac.stanford.edu", "user3@slac.stanford.edu");
    }

    @Test
    public void checkLeaderCreation() {
        var newShopGroupId = assertDoesNotThrow(
                () -> shopGroupService.createNew(
                        domainDTO.id(),
                        NewShopGroupDTO.builder()
                                .name("shop1")
                                .description("shop1 user[2-3]")
                                .users(
                                        of(
                                                ShopGroupUserInputDTO.builder()
                                                        .userId("user2@slac.stanford.edu")
                                                        .isLeader(true)
                                                        .build(),
                                                ShopGroupUserInputDTO.builder()
                                                        .userId("user3@slac.stanford.edu")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(
                authService.getAllAuthorizationForOwnerAndAndAuthTypeAndResourcePrefix(
                        "user2@slac.stanford.edu",
                        AuthorizationTypeDTO.Admin,
                        SHOP_GROUP_AUTHORIZATION_TEMPLATE.formatted(newShopGroupId),
                        empty()
                )
        ).hasSize(1);
    }

    @Test
    public void updateOk() {
        var newShopGroupId = assertDoesNotThrow(
                () -> shopGroupService.createNew(
                        domainDTO.id(),
                        NewShopGroupDTO.builder()
                                .name("shop1")
                                .description("shop1 user[2-3]")
                                .users(
                                        of(
                                                ShopGroupUserInputDTO.builder()
                                                        .userId("user2@slac.stanford.edu")
                                                        .isLeader(true)
                                                        .build(),
                                                ShopGroupUserInputDTO.builder()
                                                        .userId("user3@slac.stanford.edu")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        // update the shop group
        assertDoesNotThrow(
                () -> shopGroupService.update(
                        domainDTO.id(),
                        newShopGroupId,
                        UpdateShopGroupDTO.builder()
                                .name("shop1 updated")
                                .description("shop1 user updated")
                                .users(
                                        of(
                                                ShopGroupUserInputDTO.builder()
                                                        .userId("user1@slac.stanford.edu")
                                                        .isLeader(true)
                                                        .build()
                                        )
                                )
                                .build()
                )
        );

        assertThat(
                authService.getAllAuthorizationForOwnerAndAndAuthTypeAndResourcePrefix(
                        "user2@slac.stanford.edu",
                        AuthorizationTypeDTO.Admin,
                        SHOP_GROUP_AUTHORIZATION_TEMPLATE.formatted(newShopGroupId),
                        empty()
                )
        ).hasSize(0);
        assertThat(
                authService.getAllAuthorizationForOwnerAndAndAuthTypeAndResourcePrefix(
                        "user1@slac.stanford.edu",
                        AuthorizationTypeDTO.Admin,
                        SHOP_GROUP_AUTHORIZATION_TEMPLATE.formatted(newShopGroupId),
                        empty()
                )
        ).hasSize(1);
    }

    @Test
    public void createNewShopGroupFailsWrongUserEmail() {
        PersonNotFound personNotFoundError = assertThrows(
                PersonNotFound.class,
                () -> shopGroupService.createNew(
                        domainDTO.id(),
                        NewShopGroupDTO.builder()
                                .name("shop1")
                                .description("shop1 user[2-3]")
                                .users(
                                        of(
                                                ShopGroupUserInputDTO.builder()
                                                        .userId("user2@slac.stanford.edu")
                                                        .build(),
                                                ShopGroupUserInputDTO.builder()
                                                        .userId("bad-email@slac.stanford.edu")
                                                        .build()
                                        )
                                )
                                .build()
                )
        );
        assertThat(personNotFoundError.getErrorCode()).isEqualTo(-1);
        assertThat(personNotFoundError.getErrorMessage()).contains("bad-email@slac.stanford.edu");
    }
}
