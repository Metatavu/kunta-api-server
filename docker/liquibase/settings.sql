INSERT INTO SystemSetting (settingKey, value) VALUES ('elastic-search.hosts', 'elastic:9300');
INSERT INTO SystemSetting (settingKey, value) VALUES ('ptv.baseUrl', 'https://api.palvelutietovaranto.trn.suomi.fi');
INSERT INTO SystemSetting (settingKey, value) VALUES ('ptv.stsBaseUrl', 'https://sts.palvelutietovaranto.trn.suomi.fi');
INSERT INTO SystemSetting (settingKey, value) VALUES ('ptv.authStrategy', 'FORM');
INSERT INTO Client (name, clientId, clientSecret, accessType) VALUES ('readonly', 'READ_ONLY_ID', 'READ_ONLY_SECRET', 'READ_ONLY'), ('readwrite', 'READ_WRITE_ID', 'READ_WRITE_SECRET', 'READ_WRITE'), ('unrestricted', 'UNRESTRICTED_ID', 'UNRESTRICTED_SECRET', 'UNRESTRICTED');