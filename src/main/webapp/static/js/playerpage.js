$(function() {
	function ChartModel() {
		var self = this;
		
		var gamesSingle = makeDefStat("games", "Games")
		var gamesAcc = makeDefStat("games", "Games accumulated", true)
		var metalUsage = makeDefStat("metalUsage", "Metal usage")
		var energyUsage = makeDefStat("energyUsage", "Energy usage")
		
		self.sts = [gamesSingle, gamesAcc, metalUsage, energyUsage];
		
		var xTimeFormat = function(d) {return new Date(d*1000).toLocaleDateString()};
		

		var graphConf = {
				element : document.querySelector("#chartcontainer"),
				width : 700,
				height : 450,
				min : 'auto',
				renderer : 'bar'
		};
		
		var xAxisConf = {
			pixelsPerTick : 200,
			element : document.querySelector("#xaxis"),
			height : 20,
			tickFormat : xTimeFormat
		};
		
		var yAxisConf = {
			pixelsPerTick : 50,
			tickFormat : Rickshaw.Fixtures.Number.formatKMBT,
		};
		
		var hoverConf = {
			xFormatter: xTimeFormat,	
		};
		
		var sliderConf = {
			element: $("#slider")	
		};
		
		self.basicChart = new BasicChart({
			stats: self.sts,
			graph: graphConf,
			xAxis: xAxisConf,
			yAxis: yAxisConf,
			hover: hoverConf,
			slider: sliderConf
		});
	}
	
	var viewModel = new ChartModel();
	chartdata.timeData = chartdata.data;
	ko.applyBindings(viewModel.basicChart);
	window.setTimeout(function(){viewModel.basicChart.updateData(chartdata);}, 1000);
});