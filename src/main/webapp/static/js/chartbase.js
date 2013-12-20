function makeDefStat(name, label, accu) {
	accu = typeof accu !== 'undefined' ? accu : false;
	return {
		getValue: function(tp, i) {
			return tp[i][name];
		},
		text: label,
		accumulates: accu
	};
}

function BasicChart(config) {
	var self = this;
	self.stats = config.stats;
	self.graphConfig = config.graph;
	self.xAxisConfig = config.xAxis;
	self.yAxisConfig = config.yAxis;
	self.hoverConfig = config.hover;
	self.sliderConfig = config.slider;
	
	self.selectedStat = ko.observable(self.stats[0]);
	
	self.selectStat = function(data) {
		self.selectedStat(data);
		self.updateData(currentData);
	};
	
	var tableData = [{
		data: [{x:0, y:0}]
	}];
	
	self.graphConfig.series = tableData;
	var graph = new Rickshaw.Graph(self.graphConfig);
	self.xAxisConfig.graph = graph; 
	var xAxis = new Rickshaw.Graph.Axis.X(self.xAxisConfig);
	
	// hacky solution the the height of the axis growing and growing and
	// growing due to a bug in rickshaw (?) ...
	var oldSetSize = xAxis.setSize;
	xAxis.setSize = function(a) {
		if (oldSetSize != undefined)
			oldSetSize(a);
		oldSetSize = undefined;
	}
	
	self.yAxisConfig.graph = graph;
	var yAxis = new Rickshaw.Graph.Axis.Y(self.yAxisConfig);
	
	graph.render();
	
	self.hoverConfig.graph = graph;
	var hoverDetail = new Rickshaw.Graph.HoverDetail(self.hoverConfig);
	
	self.sliderConfig.graph = graph;
	var slider = new Rickshaw.Graph.RangeSlider(self.sliderConfig);
	
	var currentData = undefined;
	
	self.updateData = function(data) {
		currentData = data;
		var selected = self.selectedStat();
		var info = data.info;
		var palette = new Rickshaw.Color.Palette();
		
		tableData.length = 0;
		
		for (id in data.timeData) {
			var accumulator = 0;
			var idData = [];
			for (var i = 0; i < data.timeData[id].length; i++) {
				var val = selected.getValue(data.timeData[id], i);
				if (selected.accumulates) {
					accumulator += val;
					val = accumulator;
				}
				idData.push({x: data.timeData[id][i].timepoint, y: val});
			}
			var clr = palette.color();
			if (info[id].color) {
				clr = info[id].color;
			}
			tableData.push({
				name: info[id].name,
				data: idData,
				color: clr,
			});	
		}
		
		graph.update();
	}
}