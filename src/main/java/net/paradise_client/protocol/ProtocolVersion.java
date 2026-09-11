package net.paradise_client.protocol;

import com.google.common.collect.*;

import java.util.*;

public enum ProtocolVersion {
  UNKNOWN(-1, "Unknown"),
  LEGACY(-2, "Legacy"),
  V_1_8(47, "1.8", "1.8.1", "1.8.2", "1.8.3", "1.8.4", "1.8.5", "1.8.6", "1.8.7", "1.8.8", "1.8.9"),
  V_1_9(107, "1.9"),
  V_1_9_1(108, "1.9.1"),
  V_1_9_2(109, "1.9.2"),
  V_1_9_4(110, "1.9.3", "1.9.4"),
  V_1_10(210, "1.10", "1.10.1", "1.10.2"),
  V_1_11(315, "1.11"),
  V_1_11_1(316, "1.11.1", "1.11.2"),
  V_1_12(335, "1.12"),
  V_1_12_1(338, "1.12.1"),
  V_1_12_2(340, "1.12.2"),
  V_1_13(393, "1.13"),
  V_1_13_1(401, "1.13.1"),
  V_1_13_2(404, "1.13.2"),
  V_1_14(477, "1.14"),
  V_1_14_1(480, "1.14.1"),
  V_1_14_2(485, "1.14.2"),
  V_1_14_3(490, "1.14.3"),
  V_1_14_4(498, "1.14.4"),
  V_1_15(573, "1.15"),
  V_1_15_1(575, "1.15.1"),
  V_1_15_2(578, "1.15.2"),
  V_1_16(735, "1.16"),
  V_1_16_1(736, "1.16.1"),
  V_1_16_2(751, "1.16.2"),
  V_1_16_3(753, "1.16.3"),
  V_1_16_4(754, "1.16.4", "1.16.5"),
  V_1_17(755, "1.17"),
  V_1_17_1(756, "1.17.1"),
  V_1_18(757, "1.18", "1.18.1"),
  V_1_18_2(758, "1.18.2"),
  V_1_19(759, "1.19"),
  V_1_19_1(760, "1.19.1", "1.19.2"),
  V_1_19_3(761, "1.19.3"),
  V_1_19_4(762, "1.19.4"),
  V_1_20(763, "1.20", "1.20.1"),
  V_1_20_2(764, "1.20.2"),
  V_1_20_3(765, "1.20.3", "1.20.4"),
  V_1_20_5(766, "1.20.5", "1.20.6"),
  V_1_21(767, "1.21", "1.21.1"),
  V_1_21_2(768, "1.21.2", "1.21.3"),
  V_1_21_4(769, "1.21.4"),
  V_1_21_5(770, "1.21.5"),
  V_1_21_6(771, "1.21.6"),
  V_1_21_7(772, "1.21.7", "1.21.8"),
  V_1_21_9(773, "1.21.9", "1.21.10"),
  V_1_21_11(774, "1.21.11"),
  V_26_1(775, "26.1"),
  V_26_2(776, "26.2");
  public static final List<Integer> SUPPORTED_VERSION_IDS = new ArrayList<>();


  public static final ProtocolVersion MINIMUM_VERSION = V_1_8;
  public static final ProtocolVersion MAXIMUM_VERSION = values()[values().length - 1];
  public static final ImmutableMap<Integer, ProtocolVersion> ID_TO_PROTOCOL_CONSTANT;

  static {
    Map<Integer, ProtocolVersion> versions = new HashMap<>();
    for (ProtocolVersion version : values()) {
      versions.putIfAbsent(version.protocol, version);
    }

    ID_TO_PROTOCOL_CONSTANT = ImmutableMap.copyOf(versions);
  }

  static {
    SUPPORTED_VERSION_IDS.add(V_1_8.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_9.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_9_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_9_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_9_4.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_10.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_11.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_11_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_12.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_12_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_12_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_13.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_13_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_13_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_14.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_14_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_14_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_14_3.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_14_4.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_15.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_15_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_15_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_16.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_16_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_16_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_16_3.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_16_4.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_17.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_17_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_18.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_18_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_19.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_19_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_19_3.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_19_4.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_20.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_20_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_20_3.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_20_5.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21_2.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21_4.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21_5.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21_6.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21_7.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21_9.protocol);
    SUPPORTED_VERSION_IDS.add(V_1_21_11.protocol);
    SUPPORTED_VERSION_IDS.add(V_26_1.protocol);
    SUPPORTED_VERSION_IDS.add(V_26_2.protocol);
  }

  private final int protocol;
  private final String[] names;

  ProtocolVersion(int protocol, String... names) {
    this.protocol = protocol;
    this.names = names;
  }

  public static boolean isSupported(ProtocolVersion protocolVersion) {
    return isSupported(protocolVersion.getProtocol());
  }

  public static boolean isSupported(int protocol) {
    return protocol >= MINIMUM_VERSION.getProtocol() && protocol <= MAXIMUM_VERSION.getProtocol();
  }

  public int getProtocol() {
    return protocol;
  }

  public static ProtocolVersion getProtocolVersion(int protocol) {
    return ID_TO_PROTOCOL_CONSTANT.getOrDefault(protocol, UNKNOWN);
  }

  public String getMostRecentSupportedVersion() {
    return names[names.length - 1];
  }

  public List<String> getVersionsSupportedBy() {
    return ImmutableList.copyOf(names);
  }

  @Override public String toString() {
    return getVersionIntroducedIn();
  }

  public String getVersionIntroducedIn() {
    return names[0];
  }

  public enum Direction {
    TO_CLIENT, TO_SERVER;
  }
}
