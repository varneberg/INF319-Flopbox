package builder;

import client.Client;

import java.io.IOException;

public final class SecureState{
        private final static builder.SecureState INSTANCE = new builder.SecureState();
        private boolean secure;

        public SecureState(){ }

        public void setSecure(boolean secure) {
                this.secure = secure;
        }

        public static SecureState getINSTANCE() {
                return INSTANCE;
        }

        public boolean isSecure() {
                return secure;
        }
}
