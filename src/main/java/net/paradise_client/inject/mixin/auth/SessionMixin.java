package net.paradise_client.inject.mixin.auth;

import net.minecraft.client.session.Session;
import net.paradise_client.inject.accessor.SessionAccessor;
import org.spongepowered.asm.mixin.*;

import java.util.UUID;

/**
 * Mixin class for modifying the behavior of the Session class.
 * <p>
 * This class implements the SessionAccessor interface to provide access to private fields of the Session class and
 * modify the username.
 * </p>
 *
 * @author SpigotRCE
 * @since 1.0
 */
@SuppressWarnings("unused") @Mixin(Session.class) public class SessionMixin implements SessionAccessor {

  @Final @Shadow @Mutable private String username;

  @Final @Shadow @Mutable private UUID uuid;

  /**
   * Sets the username in the Session class.
   * <p>
   * This method allows modification of the private username field in the Session class through the SessionAccessor
   * interface.
   * </p>
   *
   * @param username The new username to set.
   */
  @Override public void paradiseClient$setUsername(String username) {
    this.username = username;
  }

  @Override public void paradiseClient$setUUID(UUID uuid) {
    this.uuid = uuid;
  }
}
