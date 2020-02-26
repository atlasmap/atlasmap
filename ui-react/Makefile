BUILD_DIR = ./_netlify
BUILD_DIR_TEMPLATE = ./_netlify_template
UI = .
UI_PACKAGES = ${UI}/packages

netlify:
	@echo Building Netlify website

	make netlify-clean
	make netlify-build-modules
	make netlify-build-storybooks
	make netlify-prepare-dist
	make netlify-collect-storybooks

netlify-clean:
	@echo Cleanup
	rm -rf ${BUILD_DIR}
	
netlify-build-modules:
	@echo Build all modules
	(cd ${UI}; yarn build:module)
	
netlify-build-storybooks:
	@echo Build all storybooks
	(cd ${UI}; yarn build:storybook)

netlify-prepare-dist:
	@echo Create dist
	cp -a ${BUILD_DIR_TEMPLATE} ${BUILD_DIR}

netlify-collect-storybooks:
	@echo Collect built storybooks
	cp -a ${UI_PACKAGES}/atlasmap-ui/storybook-static ${BUILD_DIR}/atlasmap-ui
	cp -a ${UI_PACKAGES}/atlasmap-core/storybook-static ${BUILD_DIR}/atlasmap-core
