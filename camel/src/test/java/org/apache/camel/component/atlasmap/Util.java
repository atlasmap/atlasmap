package org.apache.camel.component.atlasmap;

import twitter4j.Status;
import twitter4j.User;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Util {

    public static Status generateMockTwitterStatus() {
        Status status = mock(Status.class);
        User user = mock(User.class);
        when(user.getName()).thenReturn("Bob Vila");
        when(user.getScreenName()).thenReturn("bobvila1982");
        when(status.getUser()).thenReturn(user);
        when(status.getText()).thenReturn("Let's build a house!");
        return status;
    }

}
