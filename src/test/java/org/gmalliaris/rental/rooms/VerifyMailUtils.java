package org.gmalliaris.rental.rooms;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class VerifyMailUtils {

    public static String verifyMailAndExtractBody(JsonNode emailRootNode, String expectedTo, String expectedSubject) {

        var total = emailRootNode.get("total").intValue();
        var items = emailRootNode.get("items");
        var firstItem = items.get(0);
        var content = firstItem.get("Content");
        var subject = content.get("Headers").get("Subject").get(0);
        var to = content.get("Headers").get("To").get(0);
        var body = content.get("Body");

        assertTrue(total > 0);
        assertEquals(expectedTo, to.textValue());
        assertEquals(expectedSubject, subject.textValue());
        return body.toString();
    }

    public static UUID extractTokenFromConfirmationToken(String body){
        var tokenSection = body.split("</span>")[2];
        var token = tokenSection.substring(tokenSection.length() - 36);
        return UUID.fromString(token);
    }
}
