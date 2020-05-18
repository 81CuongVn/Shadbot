package com.shadorc.shadbot.listener;

import com.shadorc.shadbot.Shadbot;
import com.shadorc.shadbot.data.Config;
import com.shadorc.shadbot.db.DatabaseManager;
import com.shadorc.shadbot.db.guilds.entity.DBGuild;
import com.shadorc.shadbot.db.guilds.entity.DBMember;
import com.shadorc.shadbot.utils.DiscordUtils;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

public class MemberLeaveListener implements EventListener<MemberLeaveEvent> {

    private static final String TEXT = String.format("I'm not part of your server anymore, " +
            "thanks for having tested me!" +
            "%nIf you encountered an issue or want to let me know what could be improved, " +
            "do not hesitate to join my support server and tell me! %s", Config.SUPPORT_SERVER_URL);

    @Override
    public Class<MemberLeaveEvent> getEventType() {
        return MemberLeaveEvent.class;
    }

    @Override
    public Mono<Void> execute(MemberLeaveEvent event) {
        if (event.getUser().getId().equals(Shadbot.getSelfId())) {
            return event.getGuild()
                    .flatMap(Guild::getOwner)
                    .flatMap(Member::getPrivateChannel)
                    .onErrorResume(ClientException.class, err -> Mono.empty())
                    .flatMap(channel -> DiscordUtils.sendMessage(TEXT, channel))
                    .then();
        }

        // Delete the member from the database
        final Mono<Void> deleteMember = Mono.justOrEmpty(event.getMember())
                .flatMap(member -> DatabaseManager.getGuilds()
                        .getDBMember(member.getGuildId(), member.getId()))
                .flatMap(DBMember::delete);

        // Send an automatic leave message if one was configured
        final Mono<Message> sendLeaveMessage = DatabaseManager.getGuilds()
                .getDBGuild(event.getGuildId())
                .map(DBGuild::getSettings)
                .flatMap(settings -> Mono.zip(
                        Mono.justOrEmpty(settings.getMessageChannelId()),
                        Mono.justOrEmpty(settings.getLeaveMessage())))
                .flatMap(tuple -> MemberJoinListener.sendAutoMessage(event.getClient(), event.getUser(),
                        tuple.getT1(), tuple.getT2()));

        return deleteMember.and(sendLeaveMessage);
    }
}