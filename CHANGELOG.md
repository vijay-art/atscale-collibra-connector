# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2022-03-24

### Changed
- Added support to process Universe sub-folders and parent folders up to the "Universes" folder.
- Added support to process the Documents and their parent folders up to the "Root Folder".
- Added support to process Reports, Report Elements, and Data Providers.
- Added support for Technical Lineage.


## [v1.0.3] - 2022-01-11

### Changed
- Updated the Spring Boot Integration Library dependency version in the pom.xml file to v1.1.3 that supports the latest Collibra Platform versions (v2022.01+).


## [v1.0.2] - 2021-12-23

### Added
- Updated Log4J Apache from v2.16 to v.17


## [1.0.1] - 2021-12-16

### Changes
- Updated logger log4j2 dependency to Apache log4j2 version 2.16.0.
- Included missing dependencies.
- Default empty values for Collibra attributes in case of corresponding null values from SAP BO.
- Fixed scheduler.

## [1.0.0] - 2021-09-01

### Added
- Initial release: A Spring Boot integration that extracts SAP BusinessObject universes and other entities, transforms them and loads them into two Collibra domains. 
