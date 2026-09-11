package net.paradise_client.ui.notification;

import java.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class NotificationManager {
  private final List<Notification> notifications = new ArrayList<>();

  public void addNotification(Notification notification) {
    notifications.add(notification);
  }

  public void drawNotifications(GuiGraphicsExtractor ctx, Font tr) {
    for (int i = 0; i < notifications.size(); i++) {
      Notification n = notifications.get(i);
      if (n.draw(ctx, tr, i)) {
        notifications.remove(i--);
      }
    }
  }
}
