ALTER TABLE `OrganizationExternalAccessToken` CHANGE `tokenType` `tokenType` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `OrganizationExternalAccessToken` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Task` CHANGE `uniqueId` `uniqueId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `StoredResource` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `SystemSetting` CHANGE `settingKey` `settingKey` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `OrganizationSetting` CHANGE `settingKey` `settingKey` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `OrganizationSetting` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ClientOrganizationPermissionGrant` CHANGE `permission` `permission` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Client` CHANGE `name` `name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Client` CHANGE `clientId` `clientId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Client` CHANGE `clientSecret` `clientSecret` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Client` CHANGE `accessType` `accessType` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `StoredBinaryResource` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `StoredBinaryResource` CHANGE `contentType` `contentType` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ArchivedIdentifier` CHANGE `kuntaApiId` `kuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ArchivedIdentifier` CHANGE `source` `source` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ArchivedIdentifier` CHANGE `sourceId` `sourceId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ArchivedIdentifier` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ArchivedIdentifier` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Identifier` CHANGE `kuntaApiId` `kuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Identifier` CHANGE `source` `source` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Identifier` CHANGE `sourceId` `sourceId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Identifier` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Identifier` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `TaskQueue` CHANGE `name` `name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `TaskQueue` CHANGE `responsibleNode` `responsibleNode` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `OrganizationExternalAccessToken` CHANGE `accessToken` `accessToken` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `StoredResource` CHANGE `data` `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `SystemSetting` CHANGE `value` `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `OrganizationSetting` CHANGE `value` `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `ArchivedIdentifier` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Client` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `ClientOrganizationPermissionGrant` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Identifier` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `IdentifierRelation` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `OrganizationExternalAccessToken` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `OrganizationSetting` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `StoredBinaryResource` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `StoredResource` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `SystemSetting` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `Task` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `TaskQueue` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER DATABASE DEFAULT CHARACTER SET = utf8mb4;