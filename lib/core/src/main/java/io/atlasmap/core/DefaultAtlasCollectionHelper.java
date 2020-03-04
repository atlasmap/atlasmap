/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.core;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasCollectionHelper;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Multiplicity;

import java.util.List;

public class DefaultAtlasCollectionHelper implements AtlasCollectionHelper {

    private AtlasFieldActionService fieldActionService;

    public DefaultAtlasCollectionHelper() {
        this.fieldActionService = DefaultAtlasFieldActionService.getInstance();
    }

    public DefaultAtlasCollectionHelper(AtlasFieldActionService fieldActionService) {
        this.fieldActionService = fieldActionService;
    }

    public int determineTargetCollectionCount(Field targetField) {
        AtlasPath targetPath = new AtlasPath(targetField.getPath());
        int targetCollectionCount = targetPath.getCollectionSegmentCount();
        if (targetField.getIndex() != null) {
            targetCollectionCount++; //adjust based on index
        }
        return targetCollectionCount;
    }

    public int determineSourceCollectionCount(Field sourceParentField, Field sourceField) {
        AtlasPath sourcePath = new AtlasPath(sourceField.getPath());
        int sourceCollectionCount = sourcePath.getCollectionSegmentCount();
        sourceCollectionCount += getCollectionCountAdjustmentForActions(sourceParentField);
        sourceCollectionCount += getCollectionCountAdjustmentForActions(sourceField);
        if (sourceField.getIndex() != null) {
            sourceCollectionCount--; //adjust based on index
        }
        return sourceCollectionCount;
    }

    private int getCollectionCountAdjustmentForActions(Field sourceField) {
        int sourceCollectionCount = 0;
        if (sourceField != null && sourceField.getActions() != null) {
            for (Action action : sourceField.getActions()) {
                ActionDetail actionDetail = null;
                try {
                    actionDetail = fieldActionService.findActionDetail(action, sourceField.getFieldType());
                } catch (AtlasException e) {
                    throw new RuntimeException(e);
                }

                if (actionDetail != null) {
                    if (Multiplicity.ONE_TO_MANY.equals(actionDetail.getMultiplicity())) {
                        sourceCollectionCount++;
                    } else if (Multiplicity.MANY_TO_ONE.equals(actionDetail.getMultiplicity())) {
                        sourceCollectionCount--;
                    }
                }
            }
        }
        return sourceCollectionCount;
    }

    public void copyCollectionIndexes(Field sourceParentField, Field sourceField, Field targetField, Field previousTargetField) {
        AtlasPath sourcePath = new AtlasPath(sourceField.getPath());
        AtlasPath targetPath = new AtlasPath(targetField.getPath());
        int targetCollectionCount = determineTargetCollectionCount(targetField);
        int sourceCollectionCount = determineSourceCollectionCount(sourceParentField, sourceField);
        int targetIndex = 0;
        int collectionCount = 0;
        List<AtlasPath.SegmentContext> targetSegments = targetPath.getSegments(true);

        if (targetCollectionCount > sourceCollectionCount) {
            //Put 0 index in excessive target collections, if targetCollectionCount > sourceCollectionCount
            while (collectionCount < targetCollectionCount - sourceCollectionCount) {
                AtlasPath.SegmentContext targetSegment = targetSegments.get(targetIndex);
                if (targetSegment.getCollectionType() != CollectionType.NONE) {
                    targetPath.setCollectionIndex(targetIndex, 0);
                    collectionCount++;
                }
                targetIndex++;
            }
        }

        AtlasPath previousTargetPath = previousTargetField != null ? new AtlasPath(previousTargetField.getPath()) : null;
        List<AtlasPath.SegmentContext> sourceCollectionSegments = sourcePath.getCollectionSegments(true);
        AtlasPath.SegmentContext lastSourceSegment = sourcePath.getLastSegment();
        AtlasPath.SegmentContext lastTargetCollectionSegment = targetPath.getLastCollectionSegment();
        for (AtlasPath.SegmentContext sourceSegment : sourcePath.getSegments(true)) {
            if (sourceSegment.getCollectionType() == CollectionType.NONE && sourceSegment != lastSourceSegment) {
                //always process last segment even if not a collection (in case e.g. split has been applied)
                continue;
            }

            while (targetSegments.size() > targetIndex) {
                AtlasPath.SegmentContext targetSegment = targetSegments.get(targetIndex);
                if (targetSegment.getCollectionType() != CollectionType.NONE) {
                    collectionCount++;
                    if (sourceSegment.getCollectionIndex() != null) {
                        if (sourceCollectionCount > targetCollectionCount && targetCollectionCount == collectionCount) {
                            //if needs to flatten excessive rightmost source collections
                            int nextCollectionIndex = determineNextCollectionIndex(previousTargetPath, sourceCollectionSegments);
                            targetPath.setCollectionIndex(targetIndex, nextCollectionIndex);
                        } else {
                            targetPath.setCollectionIndex(targetIndex, sourceSegment.getCollectionIndex());
                        }

                        targetIndex++;
                        break;
                    } else if (targetSegment == lastTargetCollectionSegment && sourceParentField instanceof FieldGroup) {
                        //if the last collection target segment, but no collection index specified (e.g. after split)
                        int nextCollectionIndex = determineNextCollectionIndex(previousTargetPath, sourceCollectionSegments);
                        targetPath.setCollectionIndex(targetIndex, nextCollectionIndex);

                        targetIndex++;
                        break;
                    }
                }

                targetIndex++;
            }
        }

        targetField.setPath(targetPath.toString());
    }

    private int determineNextCollectionIndex(AtlasPath previousTargetPath, List<AtlasPath.SegmentContext> sourceCollectionSegments) {
        int nextCollectionIndex = 0;
        if (previousTargetPath != null) {
            List<AtlasPath.SegmentContext> previousTargetCollectionSegments = previousTargetPath.getCollectionSegments(true);
            boolean parentIndexesChanged = false;
            for (int i = previousTargetCollectionSegments.size() - 2; i >= 0; i--) {
                if (!previousTargetCollectionSegments.get(i).getCollectionIndex()
                    .equals(sourceCollectionSegments.get(i).getCollectionIndex())) {
                    parentIndexesChanged = true;
                    break;
                }
            }

            if (!parentIndexesChanged) {
                //determine previous collection index
                nextCollectionIndex = previousTargetCollectionSegments
                    .get(previousTargetCollectionSegments.size() - 1).getCollectionIndex();
                nextCollectionIndex++;
            }
        }
        return nextCollectionIndex;
    }
}
