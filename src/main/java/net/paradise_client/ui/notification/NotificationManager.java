package net.paradise_client.ui.notification;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.*;

public class NotificationManager {
  private final List<Notification> notifications = new ArrayList<>();

  public synchronized void addNotification(Notification notification) {
    notifications.add(notification);
  }

  public synchronized void drawNotifications(DrawContext ctx, TextRenderer tr) {
    notifications.removeIf(n -> n.draw(ctx, tr, notifications.indexOf(n)));
  }

  public synchronized boolean mouseClicked(double mouseX, double mouseY, int button) {
    for (int i = 0; i < notifications.size(); i++) {
      Notification n = notifications.get(i);
      if (n.isHovered(mouseX, mouseY)) {
        n.onClick(button);
        notifications.remove(i);
        return true;
      }
    }
    return false;
  }
}
