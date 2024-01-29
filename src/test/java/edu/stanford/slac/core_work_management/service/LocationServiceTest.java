package edu.stanford.slac.core_work_management.service;

import edu.stanford.slac.core_work_management.api.v1.dto.LocationFilterDTO;
import edu.stanford.slac.core_work_management.api.v1.dto.NewLocationDTO;
import edu.stanford.slac.core_work_management.exception.LocationNotFound;
import edu.stanford.slac.core_work_management.model.*;
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AutoConfigureMockMvc
@SpringBootTest()
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class LocationServiceTest {
    @Autowired
    LocationService locationService;
    @Autowired
    MongoTemplate mongoTemplate;

    @BeforeEach
    public void cleanCollection() {
        mongoTemplate.remove(new Query(), Location.class);
    }

    @Test
    public void testCreateNewLocation() {
        var newLocationId = assertDoesNotThrow(
                () -> locationService.createNew(
                        NewLocationDTO.builder()
                                .name("test")
                                .description("test")
                                .locationManagerUserId("adminUserid")
                                .build()
                )
        );

        assertThat(newLocationId).isNotNull();
        var newCreatedLocation = assertDoesNotThrow(
                () -> locationService.findById(
                        newLocationId
                )
        );
        assertThat(newCreatedLocation).isNotNull();
        assertThat(newCreatedLocation.id()).isEqualTo(newLocationId);
    }

    @Test
    public void testFindAllWithoutFilter() {
        for (int idx = 0; idx < 100; idx++) {
            int finalIdx = idx;
            var newLocationId = assertDoesNotThrow(
                    () -> locationService.createNew(
                            NewLocationDTO.builder()
                                    .name(String.format("%d_text", finalIdx))
                                    .description(String.format("%d_text", finalIdx))
                                    .locationManagerUserId("adminUserid")
                                    .build()
                    )
            );
            assertThat(newLocationId).isNotNull();
        }

        var foundLocations = assertDoesNotThrow(
                () -> locationService.findAll(
                        LocationFilterDTO.builder().build()
                )
        );
        assertThat(foundLocations).isNotNull().hasSize(100);

        foundLocations = assertDoesNotThrow(
                () -> locationService.findAll(
                        LocationFilterDTO
                                .builder()
                                .text("1_text")
                                .build()
                )
        );
        assertThat(foundLocations).isNotNull().hasSize(1);

        foundLocations = assertDoesNotThrow(
                () -> locationService.findAll(
                        LocationFilterDTO
                                .builder()
                                .text("1_text 2_text")
                                .build()
                )
        );
        assertThat(foundLocations).isNotNull().hasSize(2);
    }

    @Test
    public void testLocationWithParentOK() {
        var newLocationId = assertDoesNotThrow(
                () -> locationService.createNew(
                        NewLocationDTO.builder()
                                .name("test")
                                .description("test")
                                .locationManagerUserId("adminUserid")
                                .build()
                )
        );
        var newLocationWithParentId = assertDoesNotThrow(
                () -> locationService.createNew(
                        NewLocationDTO.builder()
                                .name("test")
                                .description("test")
                                .parentId(newLocationId)
                                .locationManagerUserId("adminUserid")
                                .build()
                )
        );
        assertThat(newLocationWithParentId).isNotNull();
        var newCreatedLocation = assertDoesNotThrow(
                () -> locationService.findById(
                        newLocationWithParentId
                )
        );
        assertThat(newCreatedLocation).isNotNull();
        assertThat(newCreatedLocation.id()).isEqualTo(newLocationWithParentId);
        assertThat(newCreatedLocation.parentId()).isEqualTo(newLocationId);
    }

    @Test
    public void testErrorCreatingLocationWithNotFoundParent() {
        var locationNotFoundForParent = assertThrows(
                LocationNotFound.class,
                () -> locationService.createNew(
                        NewLocationDTO.builder()
                                .name("test")
                                .description("test")
                                .parentId("bad id")
                                .locationManagerUserId("adminUserid")
                                .build()
                )
        );
        assertThat(locationNotFoundForParent).isNotNull();
    }
}