$ ->

  container = d3.select('#grid-container')
    .append('svg:svg')

  container.append('svg:g')
    .attr('transform', 'translate(1,1)')
    .attr('id', 'grid')

  container.append('svg:g')
    .attr('transform', 'translate(1,1)')
    .attr('id', 'node_vis')
    .attr('style', 'display:none')

  container.append('svg:g')
    .attr('transform', 'translate(1,1)')
    .attr('id', 'paths')
    .attr('style', 'display:none')

  width = Math.floor(($('#grid-container').width()- 1) / 20)

  window.grid = new Grid 20, width, 10
  grid.draw()
  window.paths = new Paths 20
  window.nodes = new NodeVisualization 20

  window.playback = new Playback grid, paths, nodes

  $('#generate').click ->
    $.ajax(
      "/paths",
      type: 'POST'
      contentType: 'application/json'
      dataType: 'json'
      data: JSON.stringify({
        start: grid.startNode()
        dest: grid.destNode()
        nodes: grid.clearNodes()
      })
      beforeSend: ->
        $('#buttons button').hide()
        $('#buttons p').show()
      complete: ->
        $('#buttons button').show()
        $('#buttons p').hide()
      success: (data) ->
        $('#buttons').hide()
        $('#playback').fadeIn ->
          grid.editable false
          playback.reset(data)
          $('#paths').fadeIn()
          $('#node_vis').fadeIn()
    )
    false

  $('#clear').click ->
    grid.clear()

  $('#edit').click =>
    playback.pause()
    grid.editable true
    $('#playback').hide()
    $('#playback .progress .bar').css("width", "0%")
    $('#buttons').fadeIn()
    $('#paths').fadeOut()
    $('#node_vis').fadeOut()
    false

  $('#buttons').fadeIn()

class Playback
  constructor: (@grid, @paths, @node_vis) ->

    @scrubbing = false

    $('#rewind').click @rewind
    $('#step_back').click @step_back
    $('#play').click @play
    $('#pause').click @pause
    $('#step_forward').click @step_forward
    $('#fast_forward').click @fast_forward

    $('body').mouseup @mouseup
    $('#scrub').mousedown @mousedown
    $('#scrub').mousemove @scrub

  reset: (@steps) ->
    @step = 0
    @update()
    @play()

  update: ->
    progress = @step * 100 / (@steps.length - 1)
    $('#playback .progress .bar').css("width", "#{progress}%")
    $('#progress').text("#{@step + 1}/#{@steps.length} steps")
    @paths.paths = @steps[@step].paths
    @paths.draw()

    data = @steps[@step]
    @node_vis.update @grid.startNode(), @grid.destNode(), data.current, data.open, data.closed

  step_back: =>
    @step -= 1 unless @step is 0
    @pause()
    @update()
    false

  step_forward: =>
    @step += 1 unless @step is (@steps.length - 1)
    @pause()
    @update()
    false

  rewind: =>
    @step = 0
    @pause()
    @update()
    false

  fast_forward: =>
    @step = @steps.length - 1
    @pause()
    @update()
    false

  play: =>
    if not @tick
      @tick = setInterval @next_tick, 50
      $('#play').hide()
      $('#pause').show()
    false

  pause: =>
    if @tick
      clearInterval @tick
      @tick = null
      $('#play').show()
      $('#pause').hide()
    false

  mouseup: =>
    @scrubbing = false

  mousedown: (e) =>
    @scrubbing = true
    @scrub e

  scrub: (e) =>
    if @scrubbing
      @pause()
      @step = Math.floor @steps.length * e.offsetX / $('#scrub').width()
      @update()

  next_tick: =>
    if @step is (@steps.length - 1)
      @pause()
    else
      @step += 1
      @update()

class Grid
  constructor: (@grid_size, @width, @height) ->
    @edit = true

    @points = []
    for y in [0...@height]
      for x in [0...@width]
        @points.push [x,y,'clear']

    @points[ @width + 1 ][2] = 'start'
    @points[ (@height - 2) * @width + (@width - 2) ][2] = 'dest'

    # mousedown handled only on the grid
    $('body').mouseup @mouseup

  # clear the nodes and redraw the grid
  clear: ->
    @points = ([x, y, (if c is "blocked" then "clear" else c)] for [x, y, c] in @points)
    @draw()

  # external toggle for whether the grid is editable or not
  editable: (@edit) ->

  grid: -> d3.select('#grid')

  startNode: ->
    ([x, y] for [x, y, c] in @points when c is "start")[0]

  destNode: ->
    ([x, y] for [x, y, c] in @points when c is "dest")[0]

  clearNodes: ->
    ([x, y] for [x, y, c] in @points when c isnt "blocked")

  draw: ->
    squares = @grid().selectAll('rect').data(@points, (d) -> [d[0], d[1]] )

    squares.enter()
      .append('rect')
      .attr('x', (d, i) => d[0] * @grid_size)
      .attr('y', (d, i) => d[1] * @grid_size)
      .attr('width', @grid_size)
      .attr('height', @grid_size)
      .on('mousedown', @mousedown)
      .on('mouseover', @mouseover)

    squares.attr('class', (d, i) -> d[2])

  mousedown: (d, i) =>
    return unless @edit
    square = d3.select(d3.event.target)
    @drag = square.attr 'class'
    @mouseover d, i

  mouseup: =>
    return unless @edit
    if @drag is 'start'
      start = @grid().selectAll('rect.start')
      start.attr('class', 'start')
      start.datum()[2] = 'start'
    else if @drag is 'dest'
      dest = @grid().selectAll('rect.dest')
      dest.attr('class', 'dest')
      dest.datum()[2] = 'dest'

    @drag = false
    # console.log @grid().selectAll('rect.closed').data().length

  mouseover: (d, i) =>
    return unless @edit
    square = d3.select(d3.event.target)
    switch @drag
      when 'clear'
        if square.classed('clear')
          @blockNode square
      when 'blocked'
        if square.classed('blocked')
          @clearNode square
      when 'start'
        if not square.classed('dest')
          before = @grid().selectAll('rect.start')
          before.classed('start', false)
          if before.attr('class') is ""
            @clearNode before
          square.classed('start', true)
      when 'dest'
        if not square.classed('start')
          before = @grid().selectAll('rect.dest')
          before.classed('dest', false)
          if before.attr('class') is ""
            @clearNode before
          square.classed('dest', true)

  clearNode: (selection) =>
    selection.attr('class', 'clear')
    d = selection.datum()
    d[2] = 'clear'
    selection.datum(d)

  blockNode: (selection) =>
    selection.attr('class', 'blocked')
    d = selection.datum()
    d[2] = 'blocked'
    selection.datum(d)

class NodeVisualization
  constructor: (@grid_size) ->

  viz: ->
    d3.select("#node_vis")

  update: (@start, @dest, @current, @open, @closed) ->
    points = []

    # later points override earlier ones

    if @open
      points = points.concat _.map @open, (o) -> [o[0], o[1], 'open']

    if @closed
      points = points.concat _.map @closed, (o) -> [o[0], o[1], 'closed']

    points.push [@start[0], @start[1], 'start']
    points.push [@dest[0], @dest[1], 'dest']

    if @current
      points.push [@current[0], @current[1], 'current']

    squares = @viz().selectAll('rect').data(points, (d) -> [d[0], d[1]] )

    squares.enter()
      .append('rect')
      .attr('x', (d, i) => d[0] * @grid_size)
      .attr('y', (d, i) => d[1] * @grid_size)
      .attr('width', @grid_size)
      .attr('height', @grid_size)

    squares.exit().remove()

    squares.attr('class', (d, i) -> d[2])

class Paths
  constructor: (@grid_size, @paths) ->
    # define the arrowhead
    @lines().append('svg:defs')
      .append('marker')
      .attr('id', 'arrowhead')
      .attr('orient', 'auto')
      .attr('viewBox', '0 0 10 10')
      .attr('refX', 6)
      .attr('refY', 5)
      .append('polyline')
      .attr('points', '0,0 10,5 0,10 1,5')

  lines: ->
    d3.select('#paths')

  # returns [ x1, y1, x2, y2 ]
  # path goes from center of node to a little before the center of the next
  lineSegment: (d) =>
    [ [x1, y1], [x2, y2] ] = d
    dx = x2 - x1
    dy = y2 - y1

    x1 += 0.2 * dx
    y1 += 0.2 * dy
    x2 -= 0.2 * dx
    y2 -= 0.2 * dy

    [ x1 * @grid_size + @grid_size / 2,
      y1 * @grid_size + @grid_size / 2,
      x2 * @grid_size + @grid_size / 2,
      y2 * @grid_size + @grid_size / 2 ]

  draw: ->
    lines = @lines().selectAll('line').data(@paths, JSON.stringify)

    lines.enter()
      .append('line')
      .attr('x1', (d, i) => @lineSegment(d)[0])
      .attr('y1', (d, i) => @lineSegment(d)[1])
      .attr('x2', (d, i) => @lineSegment(d)[2])
      .attr('y2', (d, i) => @lineSegment(d)[3])
      .attr('class', 'path')
      .attr('marker-end', 'url(#arrowhead)')
    lines.exit()
      .remove()

