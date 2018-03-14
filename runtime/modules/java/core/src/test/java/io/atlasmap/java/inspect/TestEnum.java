package io.atlasmap.java.inspect;

public class TestEnum {

    private TestEnumStatus status;
    @SuppressWarnings("unused")
    private TestEnumExtendedStatus extendedStatus;

    public TestEnumStatus getStatus() {
        return status;
    }

    public void setStatus(TestEnumStatus status) {
        this.status = status;
    }

}
