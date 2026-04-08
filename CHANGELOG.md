# Changelog

## Dev
### Bug Fixes
- Fixed published Maven POM metadata so ForgeGradle remapped dependency suffixes like `_mapped_...` and `_at_...` are stripped before publication.
- Fixed downstream dependency resolution failures caused by publishing remapped dependency versions for AE2, Forge, and GuideME.
