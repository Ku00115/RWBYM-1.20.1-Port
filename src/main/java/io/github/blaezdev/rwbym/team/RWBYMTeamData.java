package io.github.blaezdev.rwbym.team;

import io.github.blaezdev.rwbym.RWBYM;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-saved team and request state used by the modern Scroll GUI.
 *
 * <p>Original RWBYM stored this as a 1.12 world capability. The 1.20.1 port uses
 * {@link SavedData} on the overworld so team state persists with the server and does not
 * depend on removed world-capability APIs.</p>
 *
 * <p>Linked files: {@code RWBYMScrollScreen.java}, {@code OpenScrollScreenPacket.java}, and
 * {@code ScrollTeamActionPacket.java}.</p>
 */
public class RWBYMTeamData extends SavedData {
    private static final String NAME = RWBYM.MOD_ID + "_teams";
    private final List<List<UUID>> teams = new ArrayList<>();
    private final List<Request> requests = new ArrayList<>();

    public static RWBYMTeamData get(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        return overworld.getDataStorage().computeIfAbsent(RWBYMTeamData::load, RWBYMTeamData::new, NAME);
    }

    public static RWBYMTeamData load(CompoundTag tag) {
        RWBYMTeamData data = new RWBYMTeamData();
        ListTag teamsTag = tag.getList("teams", Tag.TAG_COMPOUND);
        for (Tag teamEntry : teamsTag) {
            CompoundTag teamTag = (CompoundTag) teamEntry;
            ListTag membersTag = teamTag.getList("members", Tag.TAG_INT_ARRAY);
            List<UUID> team = new ArrayList<>(4);
            for (Tag memberTag : membersTag) {
                team.add(NbtUtils.loadUUID(memberTag));
            }
            if (team.size() > 1) {
                data.teams.add(team);
            }
        }
        ListTag requestsTag = tag.getList("requests", Tag.TAG_COMPOUND);
        for (Tag requestEntry : requestsTag) {
            CompoundTag requestTag = (CompoundTag) requestEntry;
            if (requestTag.hasUUID("sender") && requestTag.hasUUID("receiver")) {
                data.requests.add(new Request(requestTag.getUUID("sender"), requestTag.getUUID("receiver")));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        this.teams.removeIf(team -> team.size() < 2);
        ListTag teamsTag = new ListTag();
        for (List<UUID> team : this.teams) {
            CompoundTag teamTag = new CompoundTag();
            ListTag membersTag = new ListTag();
            for (UUID member : team) {
                membersTag.add(NbtUtils.createUUID(member));
            }
            teamTag.put("members", membersTag);
            teamsTag.add(teamTag);
        }
        ListTag requestsTag = new ListTag();
        for (Request request : this.requests) {
            CompoundTag requestTag = new CompoundTag();
            requestTag.putUUID("sender", request.sender());
            requestTag.putUUID("receiver", request.receiver());
            requestsTag.add(requestTag);
        }
        tag.put("teams", teamsTag);
        tag.put("requests", requestsTag);
        return tag;
    }

    public Snapshot snapshot(ServerPlayer player) {
        UUID playerId = player.getUUID();
        List<String> teamMembers = getTeamList(playerId).stream()
                .filter(member -> !member.equals(playerId))
                .map(member -> nameFor(player.server, member))
                .toList();
        List<String> received = this.requests.stream()
                .filter(request -> request.receiver().equals(playerId))
                .map(request -> nameFor(player.server, request.sender()))
                .toList();
        List<String> sent = this.requests.stream()
                .filter(request -> request.sender().equals(playerId))
                .map(request -> nameFor(player.server, request.receiver()))
                .toList();
        return new Snapshot(teamMembers, received, sent);
    }

    public void sendRequest(ServerPlayer sender, String receiverName) {
        findPlayer(sender.server, receiverName).ifPresent(receiver -> sendRequest(sender.getUUID(), receiver.getUUID()));
    }

    public void acceptRequest(ServerPlayer receiver, String senderName) {
        findPlayer(receiver.server, senderName).ifPresent(sender -> confirmRequest(sender.getUUID(), receiver.getUUID()));
    }

    public void denyIncoming(ServerPlayer receiver, String senderName) {
        findPlayer(receiver.server, senderName).ifPresent(sender -> denyRequest(sender.getUUID(), receiver.getUUID()));
    }

    public void removeSent(ServerPlayer sender, String receiverName) {
        findPlayer(sender.server, receiverName).ifPresent(receiver -> denyRequest(sender.getUUID(), receiver.getUUID()));
    }

    public void leaveTeam(ServerPlayer player) {
        UUID playerId = player.getUUID();
        List<UUID> team = findExistingTeam(playerId);
        if (team == null) {
            return;
        }
        team.remove(playerId);
        this.teams.removeIf(entry -> entry.size() < 2);
        // Leaving a team invalidates stale invitations involving that player, matching the old capability cleanup.
        this.requests.removeIf(request -> request.sender().equals(playerId) || request.receiver().equals(playerId));
        setDirty();
    }

    private void sendRequest(UUID sender, UUID receiver) {
        if (sender.equals(receiver) || !hasRoom(sender) || !isSolo(receiver) || hasRequest(sender, receiver)) {
            return;
        }
        this.requests.add(new Request(sender, receiver));
        setDirty();
    }

    private void confirmRequest(UUID sender, UUID receiver) {
        if (!hasRequest(sender, receiver) || !hasRoom(sender) || !isSolo(receiver)) {
            return;
        }
        List<UUID> team = getTeamList(sender);
        if (!team.contains(receiver)) {
            team.add(receiver);
        }
        if (findExistingTeam(sender) == null) {
            this.teams.add(team);
        }
        denyRequest(sender, receiver);
        setDirty();
    }

    private void denyRequest(UUID sender, UUID receiver) {
        if (this.requests.removeIf(request -> request.sender().equals(sender) && request.receiver().equals(receiver))) {
            setDirty();
        }
    }

    private boolean hasRoom(UUID player) {
        return getTeamList(player).size() < 4;
    }

    private boolean isSolo(UUID player) {
        return getTeamList(player).size() == 1;
    }

    private boolean hasRequest(UUID sender, UUID receiver) {
        return this.requests.stream().anyMatch(request -> request.sender().equals(sender)
                && request.receiver().equals(receiver));
    }

    private List<UUID> getTeamList(UUID player) {
        List<UUID> team = findExistingTeam(player);
        if (team != null) {
            return team;
        }
        List<UUID> solo = new ArrayList<>(4);
        solo.add(player);
        return solo;
    }

    private List<UUID> findExistingTeam(UUID player) {
        for (List<UUID> team : this.teams) {
            if (team.contains(player)) {
                return team;
            }
        }
        return null;
    }

    private static Optional<ServerPlayer> findPlayer(MinecraftServer server, String name) {
        return server.getPlayerList().getPlayers().stream()
                .filter(player -> player.getGameProfile().getName().equalsIgnoreCase(name))
                .findFirst();
    }

    private static String nameFor(MinecraftServer server, UUID playerId) {
        ServerPlayer online = server.getPlayerList().getPlayer(playerId);
        if (online != null) {
            return online.getGameProfile().getName();
        }
        return server.getProfileCache().get(playerId)
                .map(profile -> profile.getName())
                .orElse(playerId.toString());
    }

    public record Snapshot(List<String> teamMembers, List<String> receivedRequests, List<String> sentRequests) {
    }

    private record Request(UUID sender, UUID receiver) {
    }
}
