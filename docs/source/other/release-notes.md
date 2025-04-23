# Release Notes
<!--start:changelog-header-->
## 1.0.0-SNAPSHOT (current development version)<!--end:changelog-header-->

**New Features & Major Changes**
**Internal changes & bugfixes**
- General
	- Fixed bug that auxiliary files were not loaded when starting from code with an initial model file
	- Fixed bug that caused deleting submodel-refs from AAS to fail when the submodel-ref had referredSemanticId set
- Endpoint
	- HTTP
		- URL query parameters are now correctly URL-decoded
		- Enabled `level` query parameter for calls to /submodels/{submodelIdentifier}/$reference as this is not explicitely forbidden in the specification although the parameter does not have any actual effect
		- fixed bug that disabled any HTTP PATCH request to /$value
	- OPC UA
		- Fixed error if ConceptDescription doesn't have an IdShort
