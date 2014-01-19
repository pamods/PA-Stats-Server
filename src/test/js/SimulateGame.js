var system = require('system');
var $ = require("./jquery-1.10.2.min.js");

function println(txt) {
	system.stdout.writeLine(txt);
}

var queryUrlBase = "http://127.0.0.1:8080/";
var reportVersion = 999;
var playersCount = 10;
var failTestTimeOut = 750;

function makeArmyEvent(spec, x, y, z, planetId, watchType, time) {
	return {
		spec: spec,
		x: x,
		y: y,
		z: z,
		planetId: planetId,
		watchType: watchType,
		time: time
	};
}

function StatsReportData() {
	var self = this;
	self.armyCount = 0;
	self.metalIncome = 0;
	self.energyIncome = 0;
	self.metalStored = 0;
	self.energyStored = 0;
	self.metalProducedSinceLastTick = 0;
	self.energyProducedSinceLastTick = 0;
	self.metalWastedSinceLastTick = 0;
	self.energyWastedSinceLastTick = 0;
	self.metalIncomeNet = 0;
	self.energyIncomeNet = 0;
	self.metalSpending = 0;
	self.energySpending = 0;
	self.apm = 0;
}

function ReportData() {
	var self = this;
	self.ident = "";
	self.reporterUberName = "";
	self.reporterDisplayName = "";
	self.reporterTeam = 0;
	self.observedTeams = [];
	self.showLive = true;
	self.firstStats = new StatsReportData();
	self.version = reportVersion;
	self.planet = new ReportedPlanet();
	self.paVersion = "unknown";
	self.armyEvents = [];
	self.gameStartTime = 0;
}

function ReportTeam() {
	var self = this;
	self.index = 0;
	self.primaryColor = "";
	self.secondaryColor = "";
	self.players = [];
}

function ReportPlayer() {
	var self = this;
	self.displayName = ""
}

function ReportedPlanet() {
	var self = this;
	self.seed = 0;
	self.temperature = 0;
	self.water_height = 0;
	self.radius = 0;
	self.biome = "metal";
	self.planet_name = "unknown planet";
	self.height_range = 0;
}

function RunningGameData() {
	var self = this;
	self.gameLink = 0;
	self.stats = new StatsReportData();
	self.armyEvents = [];
}

function GameSimulator(numPlayers) {
	var self = this;
	
	var packageSendDelay = 5000;
	
	self.ident = Math.random();
	self.gameStartTime = new Date().getTime();
	self.players = [];
	
	var averageOver = 12;
	var times = [];
	
	var reportedAvgs = false;
	
	self.addResponseTime = function(time) {
		if (times.length > averageOver) {
			times = times.slice(1, averageOver);
			times.push(time);
			var sum = 0;
			for (var i = 0; i < times.length; i++) {
				var t = times[i];
				sum = sum + t;
			}
			var avg = sum / times.length;
			if (avg > failTestTimeOut) {
				println("timeout");
			} else {
				// prevent too many reports of the time
				if (!reportedAvgs) {
					println("time;"+Math.floor(avg)+"ms");
				}
				reportedAvgs = true;
			}
		} else {
			times.push(time); // only start checking the avg when we have a few values
		}
	}
	
	// always FFA for now
	self.initPlayers = function(playerNum) {
		for (var i = 0; i < playerNum; i++) {
			self.players.push(new PlayerSimulator("uN "+i+"/"+Math.random(), "Player "+i, i, self));
		}
	}
	
	self.initPlayers(numPlayers);

	
	self.generateBaseInitialReport = function() {
		function generateObservedTeams() {
			var result = [];
			for (var i = 0; i < self.players.length; i++) {
				var team = new ReportTeam();
				team.index = i;
				team.primaryColor = "rgb(111,22,33)";
				team.secondaryColor = "rgb(251,25,5)";
				team.players = [];
				var player = new ReportPlayer();
				player.displayName = self.players[i].displayName;
				team.players.push(player);
				result.push(team);
			}
			return result;
		}
		
		var report = new ReportData();
		
		report.ident = self.ident+"";
		report.observedTeams = generateObservedTeams();
		report.showLive = true;
		report.paVersion = "fake";
		report.planet.seed = 1337;
		report.planet.temperature = "12";
		report.planet.water_height = "10";
		report.planet.height_range = "10";
		report.planet.radius = 10;
		report.planet.biome = "moon";
		report.planet.planet_name = "faked planet";
		return report;
	}
	
	self.sendPackages = function() {
		for (var i = 0; i < self.players.length; i++) {
			window.setTimeout(self.players[i].sendReport, 100 * i);
		}
	}
	
	self.simulate = function() {
		self.sendPackages();
		window.setTimeout(self.simulate, packageSendDelay);
	}
}

function PlayerSimulator(uberN, displayN, ix, game) {
	var self = this;
	
	self.generatedReports = 0;
	
	self.index = ix;
	
	self.gameSim = game;
	
	self.gameLinkId = undefined;
	
	self.uberName = uberN;
	self.displayName = displayN;
	
	self.nextValue = function() {
		return Math.floor(Math.random() * 1000);
	}
	
	self.generateStats = function() {
		var statsPacket = new StatsReportData();
		statsPacket.armyCount = self.nextValue();
		statsPacket.metalIncomeNet = self.nextValue();
		statsPacket.energyIncomeNet = self.nextValue();
		statsPacket.metalStored = self.nextValue();
		statsPacket.energyStored = self.nextValue();
		statsPacket.metalProducedSinceLastTick = self.nextValue();
		statsPacket.energyProducedSinceLastTick = self.nextValue();
		statsPacket.metalWastedSinceLastTick = self.nextValue();
		statsPacket.energyWastedSinceLastTick = self.nextValue();
		statsPacket.metalSpending = self.nextValue();
		statsPacket.energySpending = self.nextValue();
		statsPacket.metalIncome = self.nextValue();
		statsPacket.energyIncome = self.nextValue();
		statsPacket.apm = self.nextValue();
		return statsPacket;
	}
	
	self.generateArmyEvents = function() {
		var numEvents = Math.floor(Math.random() * self.generatedReports*2);
		var events = [];
		for (var i = 0; i < numEvents; i++) {
			var eType = Math.random() > 0.7 ? 2 : 0;
			// missle ships, missle ships, miiiiissssle ships
			events.push(makeArmyEvent("/pa/units/sea/missile_ship/missile_ship.json", 0, 1, 3, 0, eType, new Date().getTime()));
		}
		return events;
	}
	
	self.generateReport = function() {
		self.generatedReports = self.generatedReports + 1;
		if (self.gameLinkId == undefined) {
			var initialReport = self.gameSim.generateBaseInitialReport();
			initialReport.reporterUberName = self.uberName;
			initialReport.reporterDisplayName = self.displayName;
			initialReport.reporterTeam = self.index;
			initialReport.firstStats = self.generateStats();
			initialReport.armyEvents = self.generateArmyEvents();
			initialReport.gameStartTime = self.gameSim.gameStartTime
			return initialReport;
		} else {
			var runningReport = new RunningGameData();
			runningReport.gameLink = self.gameLinkId;
			runningReport.stats = self.generateStats();
			runningReport.armyEvents = self.generateArmyEvents();
			return runningReport;
		}
	}
	
	self.sendReport = function() {
		var startTime = new Date().getTime();
		$.ajax({
			type : "PUT",
			timeout: 5000,
			url : queryUrlBase + "report",
			contentType : "application/json",
			data : JSON.stringify(self.generateReport()),
			success : function(result) {
				var time = new Date().getTime() - startTime;
				self.gameSim.addResponseTime(time);
				if (self.gameLinkId === undefined) {
					self.gameLinkId = result.gameLink;
				}
			},
			error: function(x, t, m) {
				if (t == "timeout") {
					println("timeout 5k");
				}
			} 
		});
	}
}

var simGame = new GameSimulator(playersCount);
simGame.simulate();