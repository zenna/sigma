immutable Omega{T}
  intervals::Dict{Int64,T}
end

Omega() = Omega(Dict{Int64,Interval}())
Omega(::Type{EnvVar}) = Omega{EnvVar}(Dict{Int64, EnvVar}())

function getindex{T}(o::Omega{T}, key::Int64)
  if haskey(o.intervals,key)
    o.intervals[key]
  else
    i = unitinterval(T)
    o.intervals[key] = i
    i
  end
end

measure(o::Omega) = prod([measure(i) for i in values(o.intervals)]) #UNDO
# measure(o::Omega{EnvVar}) = prod([measure(i) for i in values(o.intervals)]) #UNDO
# function measure(o::Omega)
#   prod([measure(i.worlds[noconstraints]) for i in values(o.intervals)])
# end
measure(os::Vector{Omega}) = [measure(o) for o in os]
measure{T}(os::Vector{Omega{T}}) = [measure(o) for o in os]
measure(os::Vector{Omega{EnvVar}}) = [measure(o) for o in os]


to_disj_intervals(b::Box) = [IntervalDisj(b.intervals[:,i]) for i = 1:num_dims(b)]

function middle_split(o::Omega)
  ks = collect(keys(o.intervals))
  vs = collect(values(o.intervals))
  box = convert(NDimBox,vs)
  z = middle_split(box)
  map(x->Omega(Dict(ks,to_intervals(x))),z)
end

# function middle_split(o::Omega{IntervalDisj})
#   ks = collect(keys(o.intervals))
#   vs = collect(values(o.intervals))
#   box = convert(NDimBox,vs)
#   z = middle_split(box)
#   map(x->Omega(Dict(ks,to_disj_intervals(x))),z)
# end

# function middle_split(o)
#   ks = collect(keys(o.intervals))
#   vs = map(x->x.worlds[noconstraints],collect(values(o.intervals)))
#   box = convert(NDimBox,vs)
#   boxes = middle_split(box)
#   map(x->Omega(Dict(ks,convert(Vector{EnvVar},x))),boxes)
# end

middle_split(os::Vector{Omega}) = map(middle_split, os)

function rand(o::Omega)
  s = Dict{Int64,Float64}()
  for interval in o.intervals
    s[interval[1]] = rand_interval(interval[2].l,interval[2].u)
  end
  SampleOmega(s)
end

immutable SampleOmega
  samples::Dict{Int64,Float64}
end
SampleOmega() = SampleOmega(Dict{Int64,Float64}())

function getindex(o::SampleOmega, key::Int64)
  if haskey(o.samples,key)
    o.samples[key]
  else
    i = rand()
    o.samples[key] = i
    i
  end
end

function convert(::Type{Vector{Box}}, os::Vector{Omega})
  map(x->convert(NDimBox,collect(values(x.intervals))),os)
end



# Slice has a column for every dimension
# And in that column a split point
immutable Slice
  S::Vector{Float64}
  i::Int
  Slice(pre_alloc::Int) = new(Array(Float64,pre_alloc))
end


# Omega is a lazy sample space
# immutable LazyOmega
#   rootl::Float64
#   rootu::Float64
#   slices::Vector{Slice}
#   dirty::Bool
#   box::Vector{Interval}

#   LazyOmega(l,u) = new(l,u,Slice[],true,Interval[])
# end

# interval_pair(s::Float64, l::Float64, u::Float64) = [Interval(l,s),Interval(s,u)]

# # function genbox(o::LazyOmega)
# #   box = [o.rootl, o.rootu]
# #   for i = 1:length(o.slices)
# #     intervals = [split(box[j],slice[i]) for  j = 1:length(slices[i])]
# #     box = product(intervals,slice[i].i)
# #   end
# #   box
# # end

# # o = LazyOmega(0,1)
# # addslice!(o,slice([.3,.2],2))
# # o[2]

# function getindex(o::LazyOmega, key::Int)
#   # Generate Entire Box, then see if I is in resulting interval
#   # If not, then remove it
#   if o.stale
#     clean!(o)
#   end
#   x = genbox[key]
#   if

# end


