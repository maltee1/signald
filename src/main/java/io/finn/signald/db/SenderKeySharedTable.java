/*
 * Copyright 2022 signald contributors
 * SPDX-License-Identifier: GPL-3.0-only
 * See included LICENSE file
 *
 */

package io.finn.signald.db;

import io.finn.signald.Account;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.signalservice.api.SignalServiceDataStore;
import org.whispersystems.signalservice.api.SignalServiceSenderKeyStore;
import org.whispersystems.signalservice.api.push.ACI;
import org.whispersystems.signalservice.api.push.DistributionId;

public class SenderKeySharedTable {
  private static final Logger logger = LogManager.getLogger();

  private static final String TABLE_NAME = "sender_keys";
  private static final String ACCOUNT_UUID = "account_uuid";
  private static final String DISTRIBUTION_ID = "distribution_id";
  private static final String DEVICE = "device";
  private static final String ADDRESS = "address";

  private final ACI aci;

  public SenderKeySharedTable(ACI aci) { this.aci = aci; }

  public Set<SignalProtocolAddress> getSenderKeySharedWith(DistributionId distributionId) {
    Set<SignalProtocolAddress> addresses = new HashSet<>();
    try {
      PreparedStatement statement =
          Database.getConn().prepareStatement("SELECT " + ADDRESS + "," + DEVICE + ") FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + DISTRIBUTION_ID + " = ?");
      statement.setString(1, aci.toString());
      statement.setString(2, distributionId.toString());

      ResultSet rows = statement.executeQuery();
      while (rows.next()) {
        SignalProtocolAddress address = new SignalProtocolAddress(rows.getString(ADDRESS), rows.getInt(DEVICE));
        addresses.add(address);
      }
    } catch (SQLException e) {
      logger.catching(e);
    }
    return addresses;
  }

  public void markSenderKeySharedWith(DistributionId distributionId, Collection<SignalProtocolAddress> addresses) {
    try {
      PreparedStatement statement = Database.getConn().prepareStatement("INSERT OR REPLACE INTO " + TABLE_NAME + "(" + ACCOUNT_UUID + "," + ADDRESS + "," + DEVICE + "," +
                                                                        DISTRIBUTION_ID + ") VALUES (?, ?, ?)");
      for (SignalProtocolAddress address : addresses) {
        statement.setString(1, aci.toString());
        statement.setString(2, address.getName());
        statement.setInt(3, address.getDeviceId());
        statement.setString(4, distributionId.toString());
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      logger.catching(e);
    }
  }

  public void clearSenderKeySharedWith(DistributionId distributionId, Collection<SignalProtocolAddress> addresses) {
    try {
      PreparedStatement statement = Database.getConn().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE " + ACCOUNT_UUID + " = ? AND " + ADDRESS + " = ? AND " + DEVICE +
                                                                        " = ? AND " + DISTRIBUTION_ID + " = ?");
      for (SignalProtocolAddress address : addresses) {
        statement.setString(1, aci.toString());
        statement.setString(2, distributionId.toString());
        statement.setInt(3, address.getDeviceId());
        statement.setString(4, distributionId.toString());
        statement.addBatch();
      }
      statement.executeBatch();
    } catch (SQLException e) {
      logger.catching(e);
    }
  }

  public void clearSenderKeySharedWith(Collection<SignalProtocolAddress> collection) {}

  public boolean isMultiDevice() { return new Account(aci).getMultiDevice(); }

  public SignalServiceDataStore.Transaction beginTransaction() {
    return ()
               -> {
                   // No-op transaction should be safe, as it's only a performance improvement
                   // this is what signal-cli does, we should investigate eventually
               };
  }
}
