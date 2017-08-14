package io.atlasmap.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PathUtil {
    public static final String PATH_SEPARATOR = "/";
    public static final String PATH_SEPARATOR_ESCAPED = "/";
    public static final String PATH_ARRAY_START = "[";
    public static final String PATH_ARRAY_END = "]";
    public static final String PATH_LIST_START = "<";
    public static final String PATH_LIST_END = ">";
    public static final String PATH_MAP_START = "{";
    public static final String PATH_MAP_END = "}";

    private List<String> segments = new ArrayList<String>();
    private String originalPath = null;

    public PathUtil(String path) {
        this.originalPath = path;
        if (path != null && !"".equals(path)) {
            if (path.startsWith(PATH_SEPARATOR)) {
                path = path.replaceFirst(PATH_SEPARATOR, "");
            }
            if (path.contains(PATH_SEPARATOR)) {
                String[] parts = path.split(PATH_SEPARATOR_ESCAPED, 512);
                for (String part : parts) {
                    getSegments().add(part);
                }
            } else {
                getSegments().add(path);
            }
        }
    }

    private PathUtil() {
    }

    public List<SegmentContext> getSegmentContexts(boolean includeLeadingSlashSegment) {
        List<SegmentContext> contexts = new LinkedList<>();
        String segmentPath = "";
        SegmentContext previousContext = null;
        int index = 0;

        List<String> newSegments = this.getSegments();
        if (includeLeadingSlashSegment) {
            newSegments.add(0, "");
        }

        for (String s : newSegments) {
            SegmentContext c = new SegmentContext();
            segmentPath += PATH_SEPARATOR + s;
            c.setPathUtil(this);
            c.setSegment(s);
            c.setSegmentIndex(index);
            c.setSegmentPath(segmentPath);
            if (previousContext != null) {
                c.setPrev(previousContext);
                previousContext.setNext(c);

            }
            contexts.add(c);
            previousContext = c;
            if (index == 0 && includeLeadingSlashSegment) {
                segmentPath = "";
            }
            index++;
        }
        return contexts;
    }

    public PathUtil appendField(String fieldName) {
        this.segments.add(fieldName);
        return this;
    }

    public List<String> getSegments() {
        return this.segments;
    }

    public String getLastSegment() {
        if (segments.isEmpty()) {
            return null;
        } else {
            return segments.get(segments.size() - 1);
        }
    }

    public boolean hasParent() {
        return segments.size() > 1;
    }

    public static String removeCollectionIndexes(String path) {
        PathUtil pathUtil = new PathUtil(path);
        String cleanedPath = "";
        for (String s : pathUtil.getSegments()) {
            cleanedPath += PATH_SEPARATOR + removeCollectionIndex(s);
        }
        return cleanedPath;
    }

    public static String removeCollectionIndex(String segment) {
        if (segment == null) {
            return null;
        }

        if (segment.contains(PATH_ARRAY_START) && segment.contains(PATH_ARRAY_END)) {
            return segment.substring(0, segment.indexOf(PATH_ARRAY_START) + 1)
                    + segment.substring(segment.indexOf(PATH_ARRAY_END));
        }

        if (segment.contains(PATH_LIST_START) && segment.contains(PATH_LIST_END)) {
            return segment.substring(0, segment.indexOf(PATH_LIST_START) + 1)
                    + segment.substring(segment.indexOf(PATH_LIST_END));
        }

        if (segment.contains(PATH_MAP_START) && segment.contains(PATH_MAP_END)) {
            return segment.substring(0, segment.indexOf(PATH_MAP_START) + 1)
                    + segment.substring(segment.indexOf(PATH_MAP_END));
        }

        return segment;
    }

    public boolean hasCollection() {
        for (String seg : getSegments()) {
            if (isCollectionSegment(seg)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isIndexedCollection() {
        for (String seg : getSegments()) {
            if (isCollectionSegment(seg) && indexOfSegment(seg) != null) {
                return true;
            }
        }
        return false;
    }

    public Boolean isCollectionRoot() {
        return isCollectionSegment(getLastSegment());
    }

    public String getLastSegmentParent() {
        if (segments.isEmpty() || segments.size() == 1) {
            return null;
        }

        return segments.get(segments.size() - 2);
    }

    public PathUtil getLastSegmentParentPath() {
        if (segments.isEmpty() || segments.size() == 1) {
            return null;
        }

        PathUtil parentPath = new PathUtil();
        for (int i = 0; i < segments.size() - 1; i++) {
            parentPath.appendField(segments.get(i));
        }
        return parentPath;
    }

    public PathUtil deCollectionify(String collectionSegment) {
        if (segments.isEmpty() || segments.size() == 1) {
            return null;
        }

        PathUtil j = new PathUtil();
        boolean collectionFound = false;
        for (String part : segments) {
            if (collectionFound) {
                j.appendField(part);
            }
            String cleanedPart = cleanPathSegment(part);
            if (cleanedPart != null && cleanedPart.equals(cleanPathSegment(collectionSegment))) {
                collectionFound = true;
            }
        }
        return j;
    }

    public PathUtil deParentify() {
        if (segments.isEmpty() || segments.size() == 1) {
            return null;
        }

        PathUtil j = new PathUtil();
        for (int i = 1; i < segments.size(); i++) {
            j.appendField(segments.get(i));
        }
        return j;
    }

    public static String cleanPathSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }

        // strip namespace if there is one
        if (pathSegment.contains(":")) {
            pathSegment = pathSegment.substring(pathSegment.indexOf(":") + 1);
        }

        // strip leading @ symbol if there is one
        if (pathSegment.startsWith("@")) {
            pathSegment = pathSegment.substring(1);
        }

        if (pathSegment.contains(PATH_ARRAY_START) && pathSegment.endsWith(PATH_ARRAY_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(PATH_ARRAY_START, 0));
        }

        if (pathSegment.contains(PATH_LIST_START) && pathSegment.endsWith(PATH_LIST_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(PATH_LIST_START, 0));
        }

        if (pathSegment.contains(PATH_MAP_START) && pathSegment.endsWith(PATH_MAP_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(PATH_MAP_START, 0));
        }

        return pathSegment;
    }

    public static Boolean isCollectionSegment(String pathSegment) {
        if (pathSegment == null) {
            return false;
        }

        if (pathSegment.contains(PATH_ARRAY_START) && pathSegment.endsWith(PATH_ARRAY_END)) {
            return true;
        }

        if (pathSegment.contains(PATH_LIST_START) && pathSegment.endsWith(PATH_LIST_END)) {
            return true;
        }

        return pathSegment.contains(PATH_MAP_START) && pathSegment.endsWith(PATH_MAP_END);
    }

    public static Boolean isAttributeSegment(String pathSegment) {
        return pathSegment != null && pathSegment.startsWith("@");
    }

    public static Boolean isNamespaceSegment(String pathSegment) {
        return pathSegment != null && pathSegment.contains(":");
    }

    public static String getNamespace(String pathSegment) {
        if (!isNamespaceSegment(pathSegment)) {
            return null;
        }
        pathSegment = pathSegment.substring(0, pathSegment.indexOf(':'));
        if (pathSegment.startsWith("@")) {
            pathSegment = pathSegment.substring(1);
        }
        return pathSegment;
    }

    public static Integer indexOfSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }

        if (pathSegment.contains(PATH_ARRAY_START) && pathSegment.endsWith(PATH_ARRAY_END)) {
            int start = pathSegment.indexOf(PATH_ARRAY_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(PATH_ARRAY_END, start));
            if (index != null && index.length() > 0) {
                return Integer.valueOf(index);
            }
            return null;
        }

        if (pathSegment.contains(PATH_LIST_START) && pathSegment.endsWith(PATH_LIST_END)) {
            int start = pathSegment.indexOf(PATH_LIST_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(PATH_LIST_END, start));
            if (index != null && index.length() > 0) {
                return Integer.valueOf(index);
            }
            return null;
        }

        return null;
    }

    public Integer getCollectionIndex(String segment) {
        for (String part : getSegments()) {
            String cleanedPart = cleanPathSegment(part);
            if (cleanedPart != null && cleanedPart.equals(cleanPathSegment(segment))) {
                if ((part.contains(PATH_ARRAY_START) && part.contains(PATH_ARRAY_END))
                        || (part.contains(PATH_LIST_START) && (part.contains(PATH_LIST_END)))) {
                    return indexOfSegment(part);
                }
            }
        }

        return null;
    }

    public String getCollectionSegment() {
        for (String part : getSegments()) {
            if (PathUtil.isCollectionSegment(part)) {
                return part;
            }
        }
        return null;
    }

    public void setCollectionIndex(String segment, Integer index) {
        if (segment == null) {
            throw new IllegalArgumentException("PathUtil segment cannot be null");
        }

        if (index < 0) {
            throw new IllegalArgumentException("PathUtil index must be a positive integer");
        }

        if (segment.contains(PATH_ARRAY_START) && segment.contains(PATH_ARRAY_END)) {
            for (int i = 0; i < getSegments().size(); i++) {
                String part = cleanPathSegment(getSegments().get(i));
                if (part != null && part.equals(cleanPathSegment(segment))) {
                    getSegments().set(i, cleanPathSegment(segment) + PATH_ARRAY_START + index + PATH_ARRAY_END);
                }
            }
        } else if (segment.contains(PATH_LIST_START) && segment.contains(PATH_LIST_END)) {
            for (int i = 0; i < getSegments().size(); i++) {
                String part = cleanPathSegment(getSegments().get(i));
                if (part != null && part.equals(cleanPathSegment(segment))) {
                    getSegments().set(i, cleanPathSegment(segment) + PATH_LIST_START + index + PATH_LIST_END);
                }
            }
        } else {
            throw new IllegalArgumentException("PathUtil segment is not a List or Array segment");
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        int i = 0;

        if (getSegments().size() > 1) {
            builder.append(PATH_SEPARATOR);
        }

        for (String part : getSegments()) {
            builder.append(part);
            if (i < (getSegments().size() - 1)) {
                builder.append(PATH_SEPARATOR);
            }
            i++;
        }
        return builder.toString();
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public static boolean isArraySegment(String segment) {
        return isCollectionSegment(segment) && segment.contains(PATH_ARRAY_START);
    }

    public static boolean isListSegment(String segment) {
        return isCollectionSegment(segment) && segment.contains(PATH_LIST_START);
    }

    public static boolean isMapSegment(String segment) {
        return isCollectionSegment(segment) && segment.contains(PATH_MAP_START);
    }

    public static String findNextSegment(String segment, String path) {
        PathUtil pathUtil = new PathUtil(path);
        String parentSegment = null;
        for (String currentSegment : pathUtil.segments) {
            if (parentSegment == null || !segment.equals(parentSegment)) {
                parentSegment = currentSegment;
                continue;
            }
            return currentSegment;
        }
        return null;
    }

    public static class SegmentContext {

        protected String segment;
        protected String segmentPath;
        protected int segmentIndex;

        protected SegmentContext prev;
        protected SegmentContext next;
        protected PathUtil pathUtil;

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }

        public String getSegmentPath() {
            return segmentPath;
        }

        public void setSegmentPath(String segmentPath) {
            this.segmentPath = segmentPath;
        }

        public int getSegmentIndex() {
            return segmentIndex;
        }

        public void setSegmentIndex(int segmentIndex) {
            this.segmentIndex = segmentIndex;
        }

        public SegmentContext getPrev() {
            return prev;
        }

        public void setPrev(SegmentContext prev) {
            this.prev = prev;
        }

        public SegmentContext getNext() {
            return next;
        }

        public void setNext(SegmentContext next) {
            this.next = next;
        }

        public void setPathUtil(PathUtil pathUtil) {
            this.pathUtil = pathUtil;
        }

        public PathUtil getPathUtil() {
            return pathUtil;
        }

        public boolean hasParent() {
            return this.prev != null;
        }

        public boolean hasChild() {
            return this.next != null;
        }

        @Override
        public String toString() {
            return "SegmentContext [segment=" + segment + ", segmentPath=" + segmentPath + ", segmentIndex="
                    + segmentIndex + "]";
        }
    }

    public static boolean isCollection(String path) {
        return new PathUtil(path).hasCollection();
    }

    public static String overwriteCollectionIndex(String path, int index) {
        String newPath = "";
        for (SegmentContext sg : new PathUtil(path).getSegmentContexts(false)) {
            String segment = sg.getSegment();
            if (PathUtil.isCollection(segment)) {
                if (segment.contains(PATH_ARRAY_START) && segment.contains(PATH_ARRAY_END)) {
                    segment = cleanPathSegment(segment) + PATH_ARRAY_START + index + PATH_ARRAY_END;
                } else if (segment.contains(PATH_LIST_START) && segment.contains(PATH_LIST_END)) {
                    segment = cleanPathSegment(segment) + PATH_LIST_START + index + PATH_LIST_END;
                }
            }
            newPath += PATH_SEPARATOR + segment;
        }
        return newPath;
    }
}
