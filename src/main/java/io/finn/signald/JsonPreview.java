/*
 * Copyright 2022 signald contributors
 * SPDX-License-Identifier: GPL-3.0-only
 * See included LICENSE file
 *
 */

package io.finn.signald;

import io.finn.signald.annotations.Deprecated;
import io.finn.signald.exceptions.InvalidProxyException;
import io.finn.signald.exceptions.NoSuchAccountException;
import io.finn.signald.exceptions.ServerNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.push.ACI;

@Deprecated(1641027661)
public class JsonPreview {
  public String url;
  public String title;
  public JsonAttachment attachment;

  public JsonPreview(SignalServiceDataMessage.Preview preview, ACI aci)
      throws IOException, NoSuchAccountException, SQLException, InvalidKeyException, ServerNotFoundException, InvalidProxyException {
    url = preview.getUrl();
    title = preview.getTitle();
    if (preview.getImage().isPresent()) {
      attachment = new JsonAttachment(preview.getImage().get(), aci);
    }
  }
}
