package dev.rollczi.example.bukkit.chatgpt;

import dev.rollczi.example.bukkit.chatgpt.command.BanCommand;
import dev.rollczi.example.bukkit.chatgpt.command.ChatGptCommand;
import dev.rollczi.litecommands.chatgpt.ChatGptModel;
import dev.rollczi.litecommands.chatgpt.LiteChatGptExtension;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.annotations.LiteCommandsAnnotations;
import dev.rollczi.litecommands.bukkit.LiteCommandsBukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class ExampleChatGptPlugin extends JavaPlugin {

    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        this.liteCommands = LiteCommandsBukkit.builder("my-chatgpt-plugin")
            .extension(new LiteChatGptExtension<>(settings -> settings
                .apiKey("OPENAI_API_KEY") // get your api key from https://platform.openai.com/account/api-keys
                .model(ChatGptModel.GPT_4) // get model from https://platform.openai.com/docs/models/gpt-3-5
                .temperature(1.0) // see more https://platform.openai.com/docs/guides/gpt/how-should-i-set-the-temperature-parameter
                .tokensLimit(2, 64) // min and max tokens
                .cooldown(Duration.ofMillis(500)) // cooldown between suggestions per player
            ))

            .commands(LiteCommandsAnnotations.of(
                new ChatGptCommand(),
                new BanCommand()
            ))

            .build();
    }

    @Override
    public void onDisable() {
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }    
    }

}
