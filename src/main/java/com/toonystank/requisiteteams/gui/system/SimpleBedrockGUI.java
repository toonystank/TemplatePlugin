package com.toonystank.requisiteteams.gui.system;

import com.toonystank.requisiteteams.GeyserHandler;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Setter;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleBedrockGUI {

    private String title = "";
    private final List<Button> buttons = new ArrayList<>();
    private final Map<Integer, Button> buttonMap = new HashMap<>();
    @Setter
    private Runnable closeAction = null;
    private RequisitePlayer currentPlayer = null;
    private int currentPage = 0;
    private final int buttonsPerPage = 10; // Adjust based on desired form size
    private boolean isOpen = false;

    public SimpleBedrockGUI() {
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public void addButton(String displayName, Runnable action) {
        buttons.add(new Button(displayName, action));
        buttonMap.put(buttons.size() - 1, new Button(displayName, action));
    }

    public void updateButton(int index, String displayName, Runnable action) {
        if (index >= 0 && index < buttons.size()) {
            buttons.set(index, new Button(displayName, action));
            buttonMap.put(index, new Button(displayName, action));
        }
    }

    public void clear() {
        buttons.clear();
        buttonMap.clear();
    }

    public void show(RequisitePlayer player) {
        if (!GeyserHandler.isBedrockPlayer(player.getUuid())) {
            MessageUtils.debug("Player " + player.getName() + " is not a Bedrock player");
            return;
        }
        FloodgateApi api = GeyserHandler.getFloodgateApi();
        if (api == null) {
            MessageUtils.debug("Floodgate API is not initialized");
            return;
        }
        FloodgatePlayer floodgatePlayer = api.getPlayer(player.getUuid());
        if (floodgatePlayer == null) {
            MessageUtils.debug("FloodgatePlayer not found for " + player.getName());
            return;
        }
        this.currentPlayer = player;
        this.isOpen = true;
        sendForm(floodgatePlayer);
    }

    public void refresh() {
        if (currentPlayer != null && isOpen) {
            show(currentPlayer);
        }
    }

    public boolean isOpenFor(RequisitePlayer player) {
        return isOpen && currentPlayer != null && currentPlayer.equals(player);
    }

    public void close(RequisitePlayer player) {
        if (isOpenFor(player)) {
            forceClose(player);
        }
    }

    public void forceClose(RequisitePlayer player) {
        if (!isOpenFor(player)) {
            return;
        }
        FloodgateApi api = GeyserHandler.getFloodgateApi();
        if (api == null) {
            MessageUtils.debug("Floodgate API is not initialized for force close");
            return;
        }
        FloodgatePlayer floodgatePlayer = api.getPlayer(player.getUuid());
        if (floodgatePlayer == null) {
            MessageUtils.debug("FloodgatePlayer not found for " + player.getName() + " during force close");
            return;
        }
        // Send an empty form to dismiss the current form
        SimpleForm.Builder emptyForm = SimpleForm.builder()
                .title("Form Closed")
                .content("This form has been closed. Please press the close button.")
                .closedOrInvalidResultHandler((f, responseResult) -> {
                    isOpen = false;
                    if (closeAction != null) {
                        closeAction.run();
                    }
                    currentPlayer = null;
                });
        floodgatePlayer.sendForm(emptyForm);
      //  player.sendMessage("The form has been closed. Please close the window.", true);
        isOpen = false;
        if (closeAction != null) {
            closeAction.run();
        }
        currentPlayer = null;
        MessageUtils.debug("Force closed form for player: " + player.getName());
    }

    public void nextPage() {
        if ((currentPage + 1) * buttonsPerPage < buttons.size()) {
            currentPage++;
            refresh();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            refresh();
        }
    }

    public int getPagesNum() {
        return (int) Math.ceil((double) buttons.size() / buttonsPerPage);
    }

    public int getCurrentPageNum() {
        return currentPage + 1; // 1-based indexing for consistency with PaginatedGui
    }

    private void sendForm(FloodgatePlayer floodgatePlayer) {
        SimpleForm.Builder form = SimpleForm.builder()
                .title(title)
                .content(""); // Add content if needed

        // Calculate buttons for the current page
        int start = currentPage * buttonsPerPage;
        int end = Math.min(start + buttonsPerPage, buttons.size());

        // Add buttons for the current page
        for (int i = start; i < end; i++) {
            Button button = buttons.get(i);
            form.button(button.displayName);
        }

        // Add navigation buttons if needed
        if (buttons.size() > buttonsPerPage) {
            if (currentPage > 0) {
                form.button("Previous");
            }
            if (end < buttons.size()) {
                form.button("Next");
            }
        }

        // Handle form closure or invalid response
        form.closedOrInvalidResultHandler((f, responseResult) -> {
            isOpen = false;
            if (closeAction != null) {
                closeAction.run();
            }
            currentPlayer = null;
            MessageUtils.debug("Form closed or invalid for player: " + floodgatePlayer.getUsername());
        });

        // Handle valid form responses
        form.validResultHandler((f, response) -> {
            int buttonId = response.clickedButtonId();
            int adjustedButtonId = buttonId + start;

            if (buttons.size() > buttonsPerPage) {
                // Check if navigation buttons were clicked
                int navOffset = 0;
                if (currentPage > 0) {
                    if (buttonId == end - start) {
                        previousPage();
                        return;
                    }
                    navOffset++;
                }
                if (end < buttons.size()) {
                    if (buttonId == end - start + navOffset) {
                        nextPage();
                        return;
                    }
                }
                adjustedButtonId = buttonId + start;
            }

            if (adjustedButtonId >= 0 && adjustedButtonId < buttons.size()) {
                Button button = buttons.get(adjustedButtonId);
                if (button.action != null) {
                    button.action.run();
                }
            }
        });

        floodgatePlayer.sendForm(form);
        MessageUtils.debug("Sent form to player: " + floodgatePlayer.getUsername() + ", buttons: " + buttons.size() + ", page: " + (currentPage + 1));
    }

    private record Button(String displayName, Runnable action) {
            private Button(String displayName, Runnable action) {
                this.displayName = displayName != null ? displayName : "";
                this.action = action;
            }
        }
}