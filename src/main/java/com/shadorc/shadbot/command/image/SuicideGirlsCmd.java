package com.shadorc.shadbot.command.image;

import com.shadorc.shadbot.api.html.suicidegirl.SuicideGirl;
import com.shadorc.shadbot.core.command.CommandCategory;
import com.shadorc.shadbot.core.command.Context;
import com.shadorc.shadbot.core.command.GroupCmd;
import com.shadorc.shadbot.core.command.SubCmd;
import com.shadorc.shadbot.object.Emoji;
import com.shadorc.shadbot.object.RequestHelper;
import com.shadorc.shadbot.utils.ShadbotUtil;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import org.jsoup.Jsoup;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class SuicideGirlsCmd extends SubCmd {

    private static final String HOME_URL = "https://www.suicidegirls.com/photos/sg/recent/all/";

    public SuicideGirlsCmd(final GroupCmd groupCmd) {
        super(groupCmd, CommandCategory.IMAGE, "suicidegirls", "Show random image from SuicideGirls");
    }

    @Override
    public Mono<?> execute(Context context) {
        return context.isChannelNsfw()
                .flatMap(isNsfw -> {
                    if (!isNsfw) {
                        return context.createFollowupMessage(Emoji.GREY_EXCLAMATION, context.localize("must.be.nsfw"));
                    }

                    return context.createFollowupMessage(Emoji.HOURGLASS, context.localize("suicidegirls.loading"))
                            .then(SuicideGirlsCmd.getRandomSuicideGirl())
                            .flatMap(post -> context.editFollowupMessage(SuicideGirlsCmd.formatEmbed(context, post)));
                });
    }

    private static Consumer<LegacyEmbedCreateSpec> formatEmbed(Context context, SuicideGirl post) {
        return ShadbotUtil.getDefaultEmbed(
                embed -> embed.setAuthor("SuicideGirls", post.getUrl(), context.getAuthorAvatar())
                        .setDescription(context.localize("suicidegirls.name").formatted(post.getName()))
                        .setImage(post.getImageUrl()));
    }

    private static Mono<SuicideGirl> getRandomSuicideGirl() {
        return RequestHelper.request(HOME_URL)
                .map(Jsoup::parse)
                .map(SuicideGirl::new);
    }

}
