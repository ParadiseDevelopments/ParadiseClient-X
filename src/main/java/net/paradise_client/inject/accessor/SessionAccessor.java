package net.paradise_client.inject.accessor;

import java.util.UUID;

/**
 * This interface provides an accessor method for setting the username in a session. It is used in the ParadiseClient
 * Fabric mod to modify the username before it is sent to the server.
 *
 * @author Spigotrce
 * @since 1.0
 */
public interface SessionAccessor {

  /**
   * Sets the username in the session.
   *
   * @param username The new username to be set. This parameter should not be null or empty. The username should be a
   *                 valid string according to Minecraft's username requirements.
   */
  void paradiseClient$setUsername(String username);

  void paradiseClient$setUUID(UUID uuid);
}
