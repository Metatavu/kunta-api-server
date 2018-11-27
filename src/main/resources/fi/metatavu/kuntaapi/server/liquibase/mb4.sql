ALTER TABLE `organizationexternalaccesstoken` CHANGE `tokenType` `tokenType` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `organizationexternalaccesstoken` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `task` CHANGE `uniqueId` `uniqueId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `storedresource` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `systemsetting` CHANGE `settingKey` `settingKey` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `organizationsetting` CHANGE `settingKey` `settingKey` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `organizationsetting` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `clientorganizationpermissiongrant` CHANGE `permission` `permission` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `client` CHANGE `name` `name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `client` CHANGE `clientId` `clientId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `client` CHANGE `clientSecret` `clientSecret` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `client` CHANGE `accessType` `accessType` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `storedbinaryresource` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `storedbinaryresource` CHANGE `contentType` `contentType` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `archivedidentifier` CHANGE `kuntaApiId` `kuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `archivedidentifier` CHANGE `source` `source` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `archivedidentifier` CHANGE `sourceId` `sourceId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `archivedidentifier` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `archivedidentifier` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `identifier` CHANGE `kuntaApiId` `kuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `identifier` CHANGE `source` `source` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `identifier` CHANGE `sourceId` `sourceId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `identifier` CHANGE `type` `type` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `identifier` CHANGE `organizationKuntaApiId` `organizationKuntaApiId` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `taskqueue` CHANGE `name` `name` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `taskqueue` CHANGE `responsibleNode` `responsibleNode` varchar(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `organizationexternalaccesstoken` CHANGE `accessToken` `accessToken` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `storedresource` CHANGE `data` `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `systemsetting` CHANGE `value` `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `organizationsetting` CHANGE `value` `value` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `archivedidentifier` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `client` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `clientorganizationpermissiongrant` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `identifier` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `identifierrelation` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `organizationexternalaccesstoken` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `organizationsetting` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `storedbinaryresource` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `storedresource` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `systemsetting` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `task` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `taskqueue` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER DATABASE DEFAULT CHARACTER SET = utf8mb4;
