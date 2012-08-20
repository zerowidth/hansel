$ ->

  container = d3.select('#grid-container')
    .append('svg:svg')

  container.append('svg:g')
    .attr('transform', 'translate(1,1)')
    .attr('id', 'grid')

  container.append('svg:g')
    .attr('transform', 'translate(1,1)')
    .attr('id', 'paths')

  width = Math.floor(($('#grid-container').width()- 1) / 20)

  # grid = new Grid 20, width, 20
  grid = new Grid 20, 5, 3
  grid.draw()
  paths = new Paths 20

  $('#generate').submit ->
    $.ajax(
      $(this).attr('action'),
      type: 'POST'
      contentType: 'application/json'
      dataType: 'json'
      data: JSON.stringify({
        start: grid.startNode()
        dest: grid.destNode()
        nodes: grid.openNodes()
      })
      success: (data) ->
        paths.paths = data[data.length - 1].paths
        paths.draw()
    )

    false

  $('#generate').fadeIn()

class Grid
  constructor: (@grid_size, @width, @height) ->

    @points = []
    for y in [0...@height]
      for x in [0...@width]
        @points.push [x,y,'open']

    @points[ @width + 1 ][2] = 'start'
    @points[ (@height - 2) * @width + (@width - 2) ][2] = 'dest'

    # mousedown handled only on the grid
    $('body').mouseup @mouseup

  grid: -> d3.select('#grid')

  startNode: ->
    ([x, y] for [x, y, c] in @points when c is "start")[0]

  destNode: ->
    ([x, y] for [x, y, c] in @points when c is "dest")[0]

  openNodes: ->
    ([x, y] for [x, y, c] in @points when c isnt "closed")

  draw: ->
    squares = @grid().selectAll('rect').data(@points)

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
    square = d3.select(d3.event.target)
    @drag = square.attr 'class'
    @mouseover d, i

  mouseup: =>
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
    square = d3.select(d3.event.target)
    switch @drag
      when 'open'
        if square.classed('open')
          @close square
      when 'closed'
        if square.classed('closed')
          @open square
      when 'start'
        if not square.classed('dest')
          before = @grid().selectAll('rect.start')
          before.classed('start', false)
          if before.attr('class') is ""
            @open before
          square.classed('start', true)
      when 'dest'
        if not square.classed('start')
          before = @grid().selectAll('rect.dest')
          before.classed('dest', false)
          if before.attr('class') is ""
            @open before
          square.classed('dest', true)

  open: (selection) =>
    selection.attr('class', 'open')
    d = selection.datum()
    d[2] = 'open'
    selection.datum(d)

  close: (selection) =>
    selection.attr('class', 'closed')
    d = selection.datum()
    d[2] = 'closed'
    selection.datum(d)

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

