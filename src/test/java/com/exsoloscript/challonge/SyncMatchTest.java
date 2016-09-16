package com.exsoloscript.challonge;

import com.exsoloscript.challonge.guice.ChallongeTestModule;
import com.exsoloscript.challonge.guice.GuiceJUnitRunner;
import com.exsoloscript.challonge.model.Match;
import com.exsoloscript.challonge.model.Participant;
import com.exsoloscript.challonge.model.Tournament;
import com.exsoloscript.challonge.model.query.ParticipantQuery;
import com.exsoloscript.challonge.model.query.TournamentQuery;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceModules({ChallongeTestModule.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SyncMatchTest {

    private ChallongeApi challongeApi;
    private List<Participant> participants;
    private Tournament tournament;

    @Inject
    public void setChallongeApi(ChallongeApi challongeApi) {
        this.challongeApi = challongeApi;
    }

    @Before
    public void setUp() throws Exception {
        this.challongeApi.tournaments().createTournament(
                TournamentQuery.builder().setName("Test").setUrl("javatesttournament").build())
                .sync();

        this.participants = this.challongeApi.participants().bulkAddParticipants("javatesttournament",
                Lists.newArrayList(
                        ParticipantQuery.builder().setName("User1").setSeed(1).build(),
                        ParticipantQuery.builder().setName("User2").setSeed(2).build(),
                        ParticipantQuery.builder().setName("User3").setSeed(3).build(),
                        ParticipantQuery.builder().setName("User4").setSeed(4).build()
                )
        ).sync();

        this.tournament = this.challongeApi.tournaments().startTournament("javatesttournament", false, false).sync();
    }

    @Test
    public void aIndexMatches() throws Exception {
        List<Match> matches = this.challongeApi.matches().getMatches("javatesttournament").sync();
        assertTrue(matches.size() == 3);

        Match firstSeed = matches.get(0);
        Match secondSeed = matches.get(1);
        Match finalMatch = matches.get(2);

        assertEquals(this.participants.stream().filter(p -> p.name().equals("User1")).findFirst().get().id(), firstSeed.player1Id());
        assertEquals(this.participants.stream().filter(p -> p.name().equals("User4")).findFirst().get().id(), firstSeed.player2Id());
        assertEquals(this.participants.stream().filter(p -> p.name().equals("User2")).findFirst().get().id(), secondSeed.player1Id());
        assertEquals(this.participants.stream().filter(p -> p.name().equals("User3")).findFirst().get().id(), secondSeed.player2Id());
        assertEquals(null, finalMatch.player1Id());
        assertEquals(null, finalMatch.player2Id());
    }

    @After
    public void tearDown() throws Exception {
        this.challongeApi.tournaments().deleteTournament("javatesttournament");
    }
}