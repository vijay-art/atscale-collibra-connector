package com.collibra.marketplace.atscale.component;

import com.collibra.marketplace.atscale.config.ApplicationConfig;
import com.collibra.marketplace.atscale.exception.DataAlreadyExistsException;
import com.collibra.marketplace.atscale.exception.GuidException;
import com.collibra.marketplace.atscale.util.Constants;
import com.collibra.marketplace.atscale.util.CustomConstants;
import com.collibra.marketplace.library.generated.core.model.AddAssetTypeRequest;
import com.collibra.marketplace.library.generated.core.model.AddAttributeTypeRequest;
import com.collibra.marketplace.library.generated.core.model.AddDomainRequest;
import com.collibra.marketplace.library.generated.core.model.AddRelationTypeRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.collibra.marketplace.atscale.util.Constants.ALREADY_EXITS;

@AllArgsConstructor
public class GuidCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuidCreator.class);

    private final ApplicationConfig appConfig;
    private final CollibraApiHelper collibraApiHelper;

    public void createGUIDsInCollibra(ApplicationConfig appConfig) {

        LOGGER.info("Checking to see if AtScale elements with GUID's exist in Collibra");

        createDomain(appConfig.getAtscaleBusinessAssetDomainId(),
                appConfig.getAtscaleBusinessAssetDomainName(),
                appConfig.getAtscaleBusinessAssetDomainDescription(),
                appConfig.getCollibraBusinessAssetDomainType());

        createDomain(appConfig.getAtscaleDataAssetDomainId(),
                appConfig.getAtscaleDataAssetDomainName(),
                appConfig.getAtscaleDataAssetDomainDescription(),
                appConfig.getCollibraDataAssetDomainType());


        createAssetTypes();
        createAttributeTypes();
        createRelationTypes();

        LOGGER.info("Finished adding required GUID's for AtScale elements to Collibra");
    }

    private UUID getGUIDFromAssetName(String assetType) {
        for (CustomConstants.AssetType atscaleAssetType : CustomConstants.AssetType.values()) {
            if (atscaleAssetType.getName().equals(assetType)) {
                return UUID.fromString(atscaleAssetType.getId());
            }
        }
        if (assetType.equals(Constants.COLUMN)) {
            return UUID.fromString(appConfig.getCollibraAssetTypeColumn());
        }
        if (assetType.equals("Table")) {
            return UUID.fromString(appConfig.getCollibraAssetTypeTable());
        }
        if (assetType.equals("Schema")) {
            return UUID.fromString(appConfig.getCollibraAssetTypeSchema());
        }
        return null;
    }

    private void createDomain(String domainId, String domainName, String description, String domainType) {
        AddDomainRequest domainRequest = new AddDomainRequest();

        try {
            if (!collibraApiHelper.domainExists(UUID.fromString(domainId))) {
                addDomain(domainId, domainRequest, domainName, description, domainType);
                LOGGER.info("Added domain {} to Collibra platform", domainName);
            }
        } catch (Exception e) {
            LOGGER.error("An error occured while adding domain to Collibra platform ", e);
            throw new GuidException("Error while adding domain to Collibra platform");
        }
    }

    private void addDomain(String domainId, AddDomainRequest domainRequest, String domainName, String description, String domainType) throws DataAlreadyExistsException{
            try {
                domainRequest.setId(UUID.fromString(domainId));
                domainRequest.setDescription(description);
                domainRequest.setName(domainName);
                domainRequest.setCommunityId(UUID.fromString(appConfig.getAtscaleCommunityId()));
                domainRequest.setTypeId(UUID.fromString(domainType)); // Wrong. Need
                collibraApiHelper.addDomain(domainRequest);
            } catch (Exception e) {
                if (!e.getMessage().contains("resourceIdAlreadyExists") && !e.getMessage().contains(ALREADY_EXITS)) {
                    LOGGER.error(String.format("Unable to create domain '%s'", domainName));
                    throw e;
                }
            }
    }

    private void createAssetTypes() throws DataAlreadyExistsException {
        AddAssetTypeRequest assetTypeRequest = new AddAssetTypeRequest();

        for (CustomConstants.AssetType atscaleAssetType : CustomConstants.AssetType.values()) {
            try {
                assetTypeRequest.setName(atscaleAssetType.getName());
                assetTypeRequest.setId(UUID.fromString(atscaleAssetType.getId()));
                assetTypeRequest.setParentId(UUID.fromString(atscaleAssetType.getParentID()));
                assetTypeRequest.setDescription(atscaleAssetType.getDescription());
                assetTypeRequest.setSymbolType(AddAssetTypeRequest.SymbolTypeEnum.NONE);
                collibraApiHelper.addAssetType(assetTypeRequest);
                LOGGER.info("Added assetType '{}' to Collibra platform", atscaleAssetType.getName());
            } catch (Exception e) {
                if (!e.getMessage().contains(ALREADY_EXITS)) {
                    LOGGER.error(String.format("Unable to create assetType '%s': %s", atscaleAssetType.getName(), e.getMessage()));
                    throw e;
                }
            }
        }
    }

    private void createAttributeTypes() {
        AddAttributeTypeRequest attrTypeRequest = new AddAttributeTypeRequest();

        for (CustomConstants.AttributeType atscaleAttributeType : CustomConstants.AttributeType.values()) {
            try {
                attrTypeRequest.setName(atscaleAttributeType.getName());
                attrTypeRequest.setId(UUID.fromString(atscaleAttributeType.getId()));
                attrTypeRequest.setDescription(atscaleAttributeType.getDescription());
                attrTypeRequest.setIsInteger(false);
                attrTypeRequest.setStringType(AddAttributeTypeRequest.StringTypeEnum.PLAIN_TEXT);
                attrTypeRequest.setKind(AddAttributeTypeRequest.KindEnum.STRING);
                LOGGER.info("Added attribute type '{}' to Collibra platform", atscaleAttributeType.getName());
            } catch (Exception e) {
                if (!e.getMessage().contains(ALREADY_EXITS)) {
                    LOGGER.error(String.format("Unable to create attribute type '%s': %s", atscaleAttributeType.getName(), e.getMessage()));
                    throw e;
                }
            }
        }
    }

    private void createRelationTypes() {
        AddRelationTypeRequest relationTypeRequest = new AddRelationTypeRequest();

        for (CustomConstants.RelationType atscaleRelationType : CustomConstants.RelationType.values()) {
            try {
                if (!collibraApiHelper.relationTypeExists(UUID.fromString(atscaleRelationType.getId()))) {
                    addRelationType(relationTypeRequest, atscaleRelationType);
                    LOGGER.info("Added relationType '{} {} {}' to Collibra platform", atscaleRelationType.getHead(), atscaleRelationType.getRole(), atscaleRelationType.getTail());
                }
            } catch (Exception e) {
                LOGGER.error("An error occured while adding Relation-Type to Collibra platform: ", e);
                throw new GuidException("An error occured while adding Relation-Type to Collibra platform: ");
            }
        }
    }

    private void addRelationType(AddRelationTypeRequest relationTypeRequest, CustomConstants.RelationType atscaleRelationType) throws DataAlreadyExistsException{
            try {
                relationTypeRequest.setId(UUID.fromString(atscaleRelationType.getId()));
                relationTypeRequest.setSourceTypeId(getGUIDFromAssetName(atscaleRelationType.getHead()));
                relationTypeRequest.setRole(atscaleRelationType.getRole());
                relationTypeRequest.setTargetTypeId(getGUIDFromAssetName(atscaleRelationType.getTail()));
                relationTypeRequest.setCoRole(atscaleRelationType.getCoRole());
                relationTypeRequest.setDescription(atscaleRelationType.getDescription());
                collibraApiHelper.addRelationType(relationTypeRequest);
            } catch (Exception e) {
                if (!e.getMessage().contains(ALREADY_EXITS)) {
                    LOGGER.error(String.format("Unable to create relationType '%s -> %s'", atscaleRelationType.getHead(), atscaleRelationType.getTail()));
                    throw e;
                }
            }
    }
}
